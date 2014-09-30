#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <search.h>
#include "svdlib.h"

static double const STRESS_FACTOR = 100.0;
static double const RESOURCE_STRESS_QUANTUM = 5;
static double const TERM_STRESS_QUANTUM = 20;
static double const LOWER_BOUND_TERMS = 5;
static int const SV_CUT = 10;
size_t nb_resources_to_stress;
int * resources_to_stress;
size_t nb_terms_to_stress;
int * terms_to_stress;
struct indexed_double ** resources_norms;  
int nb_resources = 0;
int nb_terms = 0;
char** resources;
char** terms;

int count_char (char const * const str, char c){
  char const * p = str;
  int count = 0;
  do {
    if (*p == c) count++;
  } while (*(p++));
  return count;
}

int count_substr (char const * const str, char const * const substr){
  char const * p = str;
  int count = 0;
  while ((p = strstr(p, substr))){
    count++;
    p++;
  }
  return count;
}

void tokenize_str (char const * const str, char const * const seps, char** array){
    char* tokenstring = malloc(strlen(str) + 1);
    char* token;
    int i = 0;
    strcpy(tokenstring, str);
    token = strtok(tokenstring, seps);
    while (token != NULL){
      array[i] = malloc(strlen(token) + 1);
      strcpy(array[i], token);
      token = strtok(NULL, seps);
      i++;
    }
    free(tokenstring);
}

void tokenize_int (char const * const str, char const * const seps, int* array){
    char* tokenstring = malloc(strlen(str) + 1);
    char* token;
    int i = 0;
    int value;
    strcpy(tokenstring, str);
    token = strtok(tokenstring, seps);
    while (token != NULL){
      sscanf(token, "%d", &value);
      array[i] = value;
      token = strtok(NULL, seps);
      i++;
    }
    free(tokenstring);
}

/* euclidean norm of v of length n */
double eucl_norm (int n, double const * const v) {
  double max = 0.0;
  double sum = 0.0;
  for (int i = 0 ; i < n ; i++){
    max = fmax(fabs(v[i]),max);
  }
  for (int i = 0 ; i < n ; i++){
    sum += pow((v[i] / max), 2); 
  }
  return (max * sqrt(sum));
}

/* multiply each col k of m by s[k] 
 * the size of s must be the number of cols of m */
DMat scale_cols (DMat const m, double const * const s){
  DMat d = svdNewDMat(m->rows, m->cols);
  for (int i = 0 ; i < m->rows ; i++){
    for (int j = 0 ; j < m->cols ; j++){
      d->value[i][j] = m->value[i][j] * s[j];
    }
  }
  return d;
}

/* multiply each row k of m by s[k] 
 * the size of s must be the number of rows of m */
DMat scale_rows (DMat const m, double const * const s){
  DMat d = svdNewDMat(m->rows, m->cols);
  for (int i = 0 ; i < m->rows ; i++){
    for (int j = 0 ; j < m->cols ; j++){
      d->value[i][j] = m->value[i][j] * s[i];
    }
  }
  return d;
}

struct indexed_double {
  long index; /* original position of the vector before sorting */
  double value;
};

int compare_indexed_doubles (void const * a, void const * b){
  struct indexed_double const * da = (struct indexed_double const *) a;
  struct indexed_double const * db = (struct indexed_double const *) b;
  return (da->value > db->value) - (da->value < db->value);
}

int compare_ints (void const * a, void const * b){
  int const * ia = (int const *) a;
  int const * ib = (int const *) b;
  return (*ia - *ib);
}


struct indexed_double* eucl_norm_cols (DMat const m) {
  struct indexed_double* r;
  double* vs;
  int vs_size = m->rows * m->cols;

  r = (struct indexed_double*) malloc(sizeof(struct indexed_double) * m->cols);
  vs = (double*) malloc(sizeof(double) * vs_size);

  for (int i = 0 ; i < m->cols ; i++) {
    r[i].index = i;
  }

  for (int i = 0 ; i < m->rows ; i++) {
    for (int j = 0 ; j < m->cols ; j++) {
      vs[j * m->rows + i] = m->value[i][j];
    }
  }

  for (int i = 0 ; i < m->cols ; i++) {
    r[i].value = eucl_norm(m->rows, &vs[i * m->rows]);
  }

  free(vs);

  return r;
}

void indexed_double_sort (struct indexed_double* values, long size){
  qsort(values, size, sizeof(struct indexed_double), compare_indexed_doubles);
}

void step (DMat const m,
    struct indexed_double const * const old_resources_norms,
    struct indexed_double const * const old_terms_norms,
    int const * const resources_to_stress, size_t nb_resources_to_stress, 
    int const * const terms_to_stress, size_t nb_terms_to_stress,
    struct indexed_double ** resources_norms,
    struct indexed_double ** terms_norms,
    struct indexed_double * resources_deltas, 
    struct indexed_double * terms_deltas){

  int factor = 1;
  void * stress_term_found;
  void * stress_resource_found;

  /* build a new matrix by stressing some of the terms and resources */
  DMat d = svdNewDMat(m->rows, m->cols);
  for (int i = 0 ; i < m->rows ; i++){
    for (int j = 0 ; j < m->cols ; j++){
      stress_term_found = lfind(&j, terms_to_stress, &nb_terms_to_stress, sizeof(int), compare_ints);
      stress_resource_found = lfind(&i, resources_to_stress, &nb_resources_to_stress, sizeof(int), compare_ints);
      if ((stress_term_found != NULL) || (stress_resource_found != NULL)){
        factor = STRESS_FACTOR;
      } else {
        factor = 1.0;
      }
      d->value[i][j] = m->value[i][j] * factor;
    }
  }

  /* compute the SVD of the sparse matrix */
  SMat s = svdConvertDtoS(d);
  SVDRec svd = svdLAS2A(s, SV_CUT);

  /* in the new reduced space, scale the resources by the scaling factors of Sigma */
  DMat sut = scale_rows(svd->Ut, svd->S);

  /* in the new reduced space, scale the terms by the scaling factors of Sigma */
  DMat svt = scale_rows(svd->Vt, svd->S);

  /* distances separating the resources from the origin */
  *resources_norms = eucl_norm_cols(sut);

  /* distances separating the terms from the origin */
  *terms_norms = eucl_norm_cols(svt);

  /* deltas between the previous distances separating the resources from the origin
   * and the new distances separating the resources from the origin */
  for (int i = 0 ; i < nb_resources ; ++i){
    resources_deltas[i].index = i;
    resources_deltas[i].value = fabs((*resources_norms)[i].value - old_resources_norms[i].value);
  }

  /* deltas between the previous distances separating the terms from the origin
   * and the new distances separating the terms from the origin */
  for (int i = 0 ; i < nb_terms ; ++i){
    terms_deltas[i].index = i;
    terms_deltas[i].value = fabs((*terms_norms)[i].value - old_terms_norms[i].value);
  }

  /* free the structures */
  svdFreeDMat(d);
  svdFreeSMat(s);
  svdFreeSVDRec(svd);
  svdFreeDMat(sut);
  svdFreeDMat(svt);

}

void steps (DMat const m){

  
  size_t nb_terms_to_kill;
  int * terms_to_kill;
  void * kill_term_found;
  int new_nb_terms;
  char ** old_terms;

  DMat d;
  DMat d_temp;
  SMat s;
  SVDRec svd;
  DMat sut;
  DMat svt;
  struct indexed_double * old_resources_norms;
  struct indexed_double * old_terms_norms;
  struct indexed_double * old_terms_norms_temp;

  struct indexed_double ** terms_norms;
  struct indexed_double * resources_deltas;
  struct indexed_double * terms_deltas;

  resources_norms = (struct indexed_double **) malloc(sizeof(struct indexed_double *));
  terms_norms = (struct indexed_double **) malloc(sizeof(struct indexed_double *));

  

  /* terms far from the query */
  nb_terms_to_kill = 0;
  terms_to_kill = (int*) malloc(sizeof(int) * nb_terms_to_kill);


  d = svdNewDMat(m->rows, m->cols);
  for (int i = 0 ; i < m->rows ; i++){
    for (int j = 0 ; j < m->cols ; j++){
      d->value[i][j] = m->value[i][j];
    }
  }

  /* compute the SVD of the sparse matrix */
  s = svdConvertDtoS(d);
  svd = svdLAS2A(s, SV_CUT);

  /* in the new reduced space, scale the resources by the scaling factors of Sigma */
  sut = scale_rows(svd->Ut, svd->S);

  /* in the new reduced space, scale the terms by the scaling factors of Sigma */
  svt = scale_rows(svd->Vt, svd->S);

  /* distances separating the resources from the origin */
  old_resources_norms = eucl_norm_cols(sut);

  /* distances separating the terms from the origin */
  old_terms_norms = eucl_norm_cols(svt);

  resources_deltas = (struct indexed_double *) malloc(sizeof(struct indexed_double) * nb_resources);
  terms_deltas = (struct indexed_double *) malloc(sizeof(struct indexed_double) * nb_terms);

  /* compute the deltas between the distances separating
   * the resources (resp. terms) from the origin
   * before and after stressing the importance of new resources (resp. terms) */
  step (d, old_resources_norms, old_terms_norms,
      resources_to_stress, nb_resources_to_stress, 
      terms_to_stress, nb_terms_to_stress,
      resources_norms, terms_norms,
      resources_deltas, terms_deltas);

  indexed_double_sort(resources_deltas, nb_resources);
  indexed_double_sort(terms_deltas, nb_terms);

#if 1
  /*printf("0deltas resources:\n");
  for (int i = 0 ; i < nb_resources ; ++i){
    printf("%s : %f\n", resources[resources_deltas[i].index], resources_deltas[i].value);
  }
  printf("0deltas terms:\n");
  for (int i = 0 ; i < nb_terms ; ++i){
    printf("%s : %f\n", terms[terms_deltas[i].index], terms_deltas[i].value);
  }*/
#endif

  for (int i = 1 ; i <= 3 ; ++i){
    free(old_resources_norms);
    old_resources_norms = (*resources_norms);
    free(old_terms_norms);
    old_terms_norms_temp = (*terms_norms);

    nb_terms_to_kill = 0;
    free(terms_to_kill);
    for (int j = 0 ; terms_deltas[j].value < LOWER_BOUND_TERMS ; ++j){
      nb_terms_to_kill++; 
    }
    terms_to_kill = (int*) malloc(sizeof(int) * nb_terms_to_kill);
    //printf("kill terms:\n");
    for (int j = 0 ; j < nb_terms_to_kill ; ++j){
      terms_to_kill[j] = terms_deltas[j].index;
      //printf("%s\n", terms[terms_to_kill[j]]);
    }

    nb_resources_to_stress = i * RESOURCE_STRESS_QUANTUM;
    free(resources_to_stress);
    resources_to_stress = (int*) malloc(sizeof(int) * nb_resources_to_stress);
    for (int j = 0 ; j < nb_resources_to_stress ; ++j){
      resources_to_stress[j] = resources_deltas[nb_resources - (1 + j)].index;
    }

    nb_terms_to_stress = i * TERM_STRESS_QUANTUM;
    free(terms_to_stress);
    terms_to_stress = (int*) malloc(sizeof(int) * nb_terms_to_stress);
    for (int j = 0 ; j < nb_terms_to_stress ; ++j){
      terms_to_stress[j] = terms_deltas[nb_terms - (1 + j)].index;
    }

    /* build a new matrix by removing unimportant terms */
    new_nb_terms = nb_terms - nb_terms_to_kill;
    d_temp = svdNewDMat(nb_resources, new_nb_terms);
    for (int k = 0, m = 0 ; k < d->rows ; k++){
      for (int l = 0, n = 0 ; l < d->cols ; l++){
        kill_term_found = lfind(&l, terms_to_kill, &nb_terms_to_kill, sizeof(int), compare_ints);
        if (kill_term_found != NULL){
          continue;
        }
        d_temp->value[m][n] = d->value[k][l];
        n++;
      }
      m++;
    }
    free(d);
    d = d_temp;


    old_terms_norms = (struct indexed_double*) malloc(sizeof(struct indexed_double) * new_nb_terms);
    for (int j = 0, k = 0 ; j < nb_terms ; ++j){
      kill_term_found = lfind(&j, terms_to_kill, &nb_terms_to_kill, sizeof(int), compare_ints);
      if (kill_term_found != NULL){
        continue;
      }
      old_terms_norms[k] = old_terms_norms_temp[j];
      k++;
    }
    free(old_terms_norms_temp);

    old_terms = terms;
    terms = (char**) malloc(new_nb_terms * sizeof(char*));
    for (int j = 0, k = 0 ; j < nb_terms ; ++j){
      kill_term_found = lfind(&j, terms_to_kill, &nb_terms_to_kill, sizeof(int), compare_ints);
      if (kill_term_found != NULL){
        continue;
      }
      terms[k] = old_terms[j];
      k++;
    }
    free(old_terms);

    nb_terms = new_nb_terms;

    free(resources_deltas);
    resources_deltas = (struct indexed_double *) malloc(sizeof(struct indexed_double) * nb_resources);
    free(terms_deltas);
    terms_deltas = (struct indexed_double *) malloc(sizeof(struct indexed_double) * nb_terms);

    step (d, old_resources_norms, old_terms_norms,
        resources_to_stress, nb_resources_to_stress, 
        terms_to_stress, nb_terms_to_stress,
        resources_norms, terms_norms,
        resources_deltas, terms_deltas);

    indexed_double_sort(resources_deltas, nb_resources);
    indexed_double_sort(terms_deltas, nb_terms);

#if 1
    /*printf("%ddeltas resources:\n", i);
    for (int j = 0 ; j < nb_resources ; ++j){
      printf("%s : %f\n", resources[resources_deltas[j].index], resources_deltas[j].value);
    }
    printf("%ddeltas terms:\n", i);
    for (int j = 0 ; j < nb_terms ; ++j){
      printf("%s : %f\n", terms[terms_deltas[j].index], terms_deltas[j].value);
    }*/
#endif
  }

  indexed_double_sort(*resources_norms, nb_resources);
  /*printf("Resources norms:\n");*/
  

  /* free the structures */
  svdFreeSMat(s);
  svdFreeSVDRec(svd);
  svdFreeDMat(sut);
  svdFreeDMat(svt);
}

int main(int argc, char **argv){
	
  FILE* fp;
  char* line = NULL;
  size_t len = 0;
  ssize_t read;
  char sep = ' ';
  char* seps = " ";
  int i = 0;

  //fp = fopen("../data/queries/french_revolution/doc1/resources_terms_matrix.log", "r");
  fp = fopen(argv[1], "r");
  
  if (fp == NULL){
    fprintf(stderr, "ldsvd: failed to open the resources-terms matrix file");
    exit(EXIT_FAILURE);
  }
  /* list of resources */
  if ((read = getline(&line, &len, fp)) != -1){
    nb_resources = count_char(line, sep) + 1;
    resources = (char**) malloc(nb_resources * sizeof(char*));
    tokenize_str(line, seps, resources);
  } else {
    fprintf(stderr, "ldsvd: failed to read the resources");
    exit(EXIT_FAILURE);
  }
  /* list of terms */
  if ((read = getline(&line, &len, fp)) != -1){
    nb_terms = count_char(line, sep) + 1;
    terms = (char**) malloc(nb_terms * sizeof(char*));
    tokenize_str(line, seps, terms);
  } else {
    fprintf(stderr, "ldsvd: failed to read the terms");
    exit(EXIT_FAILURE);
  }
  /* size of matrix */
  if ((read = getline(&line, &len, fp)) != -1){
  } else {
    fprintf(stderr, "ldsvd: failed to read the size of the matrix");
    exit(EXIT_FAILURE);
  }

  DMat d = svdNewDMat(nb_resources, nb_terms);
  if (!d) {
    fprintf(stderr, "ldsvd: failed to allocate D");
    exit(EXIT_FAILURE);
  }

  /* matrix line by line*/
  for (i = 0 ; i < nb_resources ; i++){
    read = getline(&line, &len, fp);
    if (read == -1) {
      fprintf(stderr, "ldsvd: error while reading a line of the matrix");
      exit(EXIT_FAILURE);
    }
    int values[nb_terms];
    tokenize_int (line, seps, values);
    for (int j = 0 ; j < nb_terms ; j++){
      d->value[i][j] = (double)values[j];
    }
  }
  
  /* terms and resources close to the query */
  if ((read = getline(&line, &len, fp)) != -1){
    nb_resources_to_stress = count_char(line, sep) + 1;
    resources_to_stress = (int**) malloc(nb_resources_to_stress * sizeof(int));
    tokenize_int(line, seps, resources_to_stress);
  } else {
    fprintf(stderr, "ldsvd: failed to read resources close to the query ");
    exit(EXIT_FAILURE);
  }
  
 if ((read = getline(&line, &len, fp)) != -1){
    nb_terms_to_stress = count_char(line, sep) + 1;
    terms_to_stress = (int**) malloc(nb_terms_to_stress * sizeof(int));
    tokenize_int(line, seps, terms_to_stress);
  } else {
    fprintf(stderr, "ldsvd: failed to read resources close to the query ");
    exit(EXIT_FAILURE);
  } 
   
  fclose(fp);
  steps(d);
  printf("Start listing resources\n");
  int max=50;
  if(nb_resources<50)
	max=nb_resources;
 for (int i = nb_resources-max ; i < nb_resources ; ++i) {
    printf("%s\n", resources[(*resources_norms)[i].index]);
  }
  exit(EXIT_SUCCESS);
}
