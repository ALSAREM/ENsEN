/*
 * Copyright 2014 Pierre-Edouard Portier, Mazen Alsarem
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <search.h>
#include <time.h>
#include <unistd.h>
#include "dbg.h"
#include "svdlib.h"
#include "str.h"
#include "ldsvd.h"

/* only sv_cut singular values are kept */
int sv_cut = 3;
/* acceleration_threshold is used to determine when new resources can be 
 * inluded to the list of resources to be stressed, i.e. when adding more 
 * stress to the current list of resources to be stressed wouldn't essentially 
 * change the ranking of the resources */
int acceleration_threshold = 300;

/* nb_dim is the minimum of the number of resources (nb_res) and the parameter 
 * sv_cut which is the number of singular values kept */
size_t nb_dim;
size_t nb_res = 0; /* nb_res is the number of resources */
size_t nb_ter = 0; /* nb_ter is the number of terms (or stems) */

/* res is an array of resources' names (i.e. URIs) */
char** res;
/* ter is an array of terms' names (in fact the terms are stems) */
char** ter;

/* the user can provide indices of resources to be found in the res array that 
 * he knows to be important because they are semantically close to the query 
 * similarly for the terms */
size_t nb_res_close_to_query;
size_t nb_ter_close_to_query;
size_t* res_close_to_query;
size_t* ter_close_to_query;
/* res_stress_factors is an array of size nb_res of integers. 
 * Thus, to each resource of the res array is associated an integer called its 
 * stress factor. Similarly for the terms.
 * At the beginning of each step of the ldsvd algorithm, 
 * before actually computing the SVD, each cell of the resource-term matrix is 
 * multiplied by the maximum of the corresponding resource stress factor and 
 * term stress factor. It allows to better approximate in the reduced space the 
 * resources that are similar to the stressed resources. In this context, a 
 * resource is said to be similar to the stressed resources if its directions 
 * of greatest variation tend to be the same as the ones of the stressed 
 * resources. */
int* res_stress_factors;
int* ter_stress_factors;
int nb_iter;
/* res_norms and prev_res_norms are arrays of double representing the 
 * euclidean norms of the resources at current and previous steps 
 * similarly for the terms */
double* prev_res_norms = NULL;
double* prev_ter_norms = NULL;
double* res_norms = NULL;
double* ter_norms = NULL;
/* res_deltas (and res_deltas_s s for sorted) is an array of the differences 
 * between the norms of the resources at previous iteration and the norms of 
 * the resources at current iteration.
 * similarly for ter_deltas (and ter_deltas_s) */
IDouble* res_deltas_s;
IDouble* ter_deltas_s;
double* res_deltas;
double* ter_deltas; 
/* sum_res_deltas is an array of the sum on all iterations of the relative 
 * distance the resources moved from the origin of the reduced space */
double* sum_res_deltas = NULL;
IDouble* sum_res_deltas_s = NULL;
/* resources-terms matrix */
DMat d;
/* matrix S^-1 * Vt used to transform a resource vector (a line of A) into
 * a topic vector (line of U). Indeed:
 * A = U S Vt
 * A V S^-1 = U
 * Ut = S^-1 Vt At
 * Thus, given a vector x in the original space,
 * S^-1 Vt xt is a representation of x in the reduced space. */
DMat sm1vt = NULL;
/* SVD result */
SVDRec svd = NULL;
/* adj is the resources-resources adjacency matrix 
 * adj[i][0] is the number of resources pointed to by the resource i
 * the rest of the values of adj[i] are identifiers to the resources 
 * pointed to by the resource i*/
size_t** adj;

/* euclidean norm of v of length n 
 * v is considered to be a dense vector
 * i.e. we don't optimize for sparse vectors since the norm will be used for 
 * vectors obtained after a SVD (these vectors are dense)*/
double 
eucl_norm (size_t n, double const * const v) 
{
  double max = 0.0;
  double sum = 0.0;
  for (size_t i = 0 ; i < n ; i++)
  {
    max = fmax(fabs(v[i]),max);
  }
  if (max < 1E-10) return 0.0;
  for (size_t i = 0 ; i < n ; i++)
  {
    sum += pow((v[i] / max), 2); 
  }
  return (max * sqrt(sum));
}

/* Multiply each col k of m by s[k].
 * The size of s must be the number of cols of m.
 * This operation is applied on the results of a SVD. Thus, the parameter m 
 * is a dense matrix. */
DMat 
scale_cols (DMat const m, double const * const s)
{
  DMat d = svdNewDMat(m->rows, m->cols);
  for (size_t i = 0 ; i < m->rows ; i++)
  {
    for (size_t j = 0 ; j < m->cols ; j++)
    {
      d->value[i][j] = m->value[i][j] * s[j];
    }
  }
  return d;
}

/* Multiply each row k of m by s[k].
 * The size of s must be the number of rows of m.
 * This operation is applied on the results of a SVD. Thus, the parameter m 
 * is a dense matrix. */
DMat 
scale_rows (DMat const m, double const * const s)
{
  DMat d = svdNewDMat(m->rows, m->cols);
  for (size_t i = 0 ; i < m->rows ; i++)
  {
    for (size_t j = 0 ; j < m->cols ; j++)
    {
      d->value[i][j] = m->value[i][j] * s[i];
    }
  }
  return d;
}

int 
compare_idouble_value (void const * a, void const * b)
{
  IDouble const * da = (IDouble const *) a;
  IDouble const * db = (IDouble const *) b;
  return (da->value > db->value) - (da->value < db->value);
}

int 
compare_size_t (void const * a, void const * b)
{
  size_t const * ia = (size_t const *) a;
  size_t const * ib = (size_t const *) b;
  return (*ia - *ib);
}

/* eucl_norm_cols returns an array of the euclidean norms of the columns of m*/
double* 
eucl_norm_cols (DMat const m) 
{
  /* r is the returned value */
  double* r;
  /* vs is an array that represents a dense view of the matrix m,
   * columns by columns, in a contiguous zone of the memory. */
  double* vs;
  size_t vs_size = m->rows * m->cols;

  r = (double*) malloc(sizeof(double) * m->cols);
  check_mem(r);
  vs = (double*) malloc(sizeof(double) * vs_size);
  check_mem(vs);

  for (size_t i = 0 ; i < m->rows ; i++) 
  {
    for (size_t j = 0 ; j < m->cols ; j++) 
    {
      vs[j * m->rows + i] = m->value[i][j];
    }
  }

  for (size_t i = 0 ; i < m->cols ; i++) 
  {
    r[i] = eucl_norm(m->rows, &vs[i * m->rows]);
  }

  free(vs);
  return r;

error:
  exit(EXIT_FAILURE);
}

void 
idouble_sort(IDouble* values, long size)
{
  qsort(values, size, sizeof(IDouble), compare_idouble_value);
}

/* svd_norms computes the SVD of the matrix m.
 * m is a densed representation of a sparse matrix.
 * The SVD computation is made efficiently on a sparse representation of m.
 * When the procedure returns, 
 * res_norms is an array of the norms of the resources 
 * in the reduced svd space.
 * prev_res_norms is an array of the norms of the resources
 * in the reduced svd space at the previous iteration.
 * similarly for ter_norms and prev_ter_norms */
void 
svd_norms (DMat const m)
{
  /* S^-1 */
  double* sm1;

  /* compute the SVD of the sparse matrix */
  SMat s = svdConvertDtoS(m);
  svdFreeSVDRec(svd); 
  svd = svdLAS2A(s, nb_dim);

  /* in the new reduced space, 
   * scale the resources by the scaling factors of Sigma */
  DMat sut = scale_rows(svd->Ut, svd->S);

  /* in the new reduced space, 
   * scale the terms by the scaling factors of Sigma */
  DMat svt = scale_rows(svd->Vt, svd->S);

  sm1 = (double*) malloc(sizeof(double) * nb_dim);
  check_mem(sm1);
  for (size_t j = 0 ; j < nb_dim ; ++j)
  {
    sm1[j] = 1.0 / (svd->S)[j];
  }
  /* S^-1 * Vt the operator used to convert a resource represented as a 
   * combination of stems (line of A) into a resource represented as a 
   * combination of topics (line of U) */
  svdFreeDMat(sm1vt);
  sm1vt = scale_rows(svd->Vt, sm1); 

  /* distances separating the resources from the origin */
  free(prev_res_norms);
  prev_res_norms = res_norms;
  res_norms = eucl_norm_cols(sut);

  /* distances separating the terms from the origin */
  free(prev_ter_norms);
  prev_ter_norms = ter_norms;
  ter_norms = eucl_norm_cols(svt);

  svdFreeSMat(s);
  svdFreeDMat(sut);
  svdFreeDMat(svt);
  free(sm1);
  return;

error:
  exit(EXIT_FAILURE);
}

void 
step ()
{

  /* build a new matrix by stressing some of the terms and resources */
  DMat m = svdNewDMat(d->rows, d->cols);
  for (size_t i = 0 ; i < d->rows ; i++)
  {
    for (size_t j = 0 ; j < d->cols ; j++)
    {
      if (ter_stress_factors[j] == 0 || res_stress_factors[i] == 0) 
      {
        m->value[i][j] = 0.0;
      } 
      else 
      {
        m->value[i][j] = d->value[i][j] * max(res_stress_factors[i],
                                              ter_stress_factors[j]);
      }
    }
  }

  svd_norms(m);

  /* deltas between the previous distances separating the resources from the
   * origin and the new distances separating the resources from the origin */
  for (size_t i = 0 ; i < nb_res ; ++i)
  {
    res_deltas_s[i].index = i;
    res_deltas[i] = res_norms[i] - prev_res_norms[i];
    res_deltas_s[i].value = res_deltas[i];
  }

  /* deltas between the previous distances separating the terms from the origin
   * and the new distances separating the terms from the origin */
  for (size_t i = 0 ; i < nb_ter ; ++i)
  {
    ter_deltas_s[i].index = i;
    ter_deltas[i] = ter_norms[i] - prev_ter_norms[i];
    ter_deltas_s[i].value = ter_deltas[i];
  }

  idouble_sort(res_deltas_s, nb_res);
  idouble_sort(ter_deltas_s, nb_ter);

  svdFreeDMat(m);
}

void 
steps ()
{
  
  size_t nb_res_to_stress = nb_res_close_to_query;
  size_t new_nb_res_to_stress;
  size_t* res_to_stress = NULL;
  size_t nb_ter_to_stress = nb_ter_close_to_query;
  size_t* ter_to_stress = NULL;
  /* res_to_stress_cos stores the cosines between each resource that will be 
   * stressed at the next iteration and the qk vector
   * the bigger a cosine is the bigger the stress increment for this 
   * resource will be*/
  IDouble* res_to_stress_cos = NULL;
  /* adj_res_to_stress stores the ids of the resources adjacent in the RDF 
   * graph to the resources that will be stressed
   * Thus, nb_adj_res_to_stress is new_nb_res_to_stress - nb_res_to_stress */
  size_t* adj_res_to_stress = NULL;
  size_t nb_adj_res_to_stress;
  /* adj_res_found is used to store the result of a call to lfind
   * used to find if a given resource adjacent in the RDF graph to 
   * a resource that will be stressed is itself already a resource 
   * that will be stressed (adj_res_found0) or if it was already encountered 
   * as adjacent to a previous to-be-stressed resource in which case 
   * it shouldn't be counted twice (adj_res_found1) */
  void* adj_res_found0 = NULL;
  void* adj_res_found1 = NULL;
  /* rid can be used to store a resource id, i.e. an indexed in res array */
  size_t rid;
  /* prev_res_deltas stores for each resource the distance it moved from the 
   * origin of the reduced space between the steps n-1 and n-2 (res_deltas 
   * stores the distance a resource moved between the steps n and n-1)
   * similarly for the terms with prev_ter_deltas */
  double* prev_res_deltas = NULL;
  double* prev_ter_deltas = NULL;
  /* res_deltas_deltas is used to store the difference between res_deltas and 
   * prev_res_deltas ; it is a second order difference, it can be seen as the 
   * acceleration of the resources in their movement relatively to the origin 
   * of the reduced space. 
   * res_acceleration is single value used the represent a synthesis of the 
   * accelerations of each individual resource. The formula used is the 
   * square root of the sum of the squares. */
  double* res_deltas_deltas = NULL;
  double res_acceleration;
  /* qk is a vector (array) of length nb_dim
   * It lies in the reduced space and is computed so as to point in a 
   * "good" direction, i.e. a direction that should be close to the one 
   * corresponding to the information need (given the query, the RDF graph, and 
   * the text associated to each concept) */
  double* qk = NULL;
  /* u is an array of length nb_dim used to store a row of U */
  double* u = NULL;
  /* s_product stores the scalar product of a u vector with the qk vector */
  double s_product;
  /* cosine stores the cosine corresponding to s_product */
  double cosine;

  ter_to_stress = (size_t*) malloc(sizeof(size_t) * nb_ter);
  check_mem(ter_to_stress);
  res_to_stress = (size_t*) malloc(sizeof(size_t) * nb_res);
  check_mem(res_to_stress);
  prev_res_deltas = (double*) malloc(sizeof(double) * nb_res);
  check_mem(prev_res_deltas);
  prev_ter_deltas = (double*) malloc(sizeof(double) * nb_ter);
  check_mem(prev_ter_deltas);
  res_deltas_deltas = (double*) malloc(sizeof(double) * nb_res);
  check_mem(res_deltas_deltas);
  sum_res_deltas = (double*) malloc(sizeof(double) * nb_res);
  check_mem(sum_res_deltas);
  sum_res_deltas_s = (IDouble*) malloc(sizeof(IDouble) * nb_res);
  check_mem(sum_res_deltas_s);
  adj_res_to_stress = (size_t*) malloc(sizeof(size_t) * nb_res);
  check_mem(adj_res_to_stress);
  qk = (double*) malloc(sizeof(double) * nb_dim);
  check_mem(qk);
  u = (double*) malloc(sizeof(double) * nb_dim);
  check_mem(u);
  res_to_stress_cos = (IDouble*) malloc(sizeof(IDouble) * nb_res);
  check_mem(res_to_stress_cos);

  for (size_t j = 0 ; j < nb_res ; ++j)
  {
    sum_res_deltas[j] = res_deltas[j];
  }

  for (int i = 0;;++i)
  {
    debug("DEBUG iteration %d", i);
    nb_iter = i+1;
    
    for (size_t j = 0 ; j < nb_res ; ++j)
    {
      sum_res_deltas_s[j].index = j;
      sum_res_deltas_s[j].value = sum_res_deltas[j];
    }
    idouble_sort(sum_res_deltas_s, nb_res);

    /* qk is made of the sum of the vector of the current best resource in the 
     * reduced space and of the vector made from an equal participation of each 
     * best term */
    for (size_t j = 0 ; j < nb_dim ; ++j)
    {
      qk[j] = svd->Ut->value[j][sum_res_deltas_s[nb_res - 1].index];
      for (size_t k = 0 ; k < nb_ter_to_stress ; ++k)
      {
        qk[j] += sm1vt->value[j][ter_deltas_s[nb_ter - (1 + k)].index];
      }
    }

    for (size_t j = 0 ; j < nb_ter_to_stress ; ++j)
    {
      ter_to_stress[j] = ter_deltas_s[nb_ter - (1 + j)].index;
    }

    /* nb_res_to_stress is the new number of resources to stress when 
    * not taking into account: the resources that (i) are linked to the 
    * nb_res_to_stress resources moved by the stressing process,
    * but (ii) are not part of these nb_res_to_stress resources. */
    for (size_t j = 0 ; j < nb_res_to_stress ; ++j)
    {
      rid = sum_res_deltas_s[nb_res - (1 + j)].index;
      s_product = 0.0;
      for (size_t k = 0 ; k < nb_dim ; ++k)
      {
        u[k] = (svd->Ut)->value[k][rid];
        s_product += u[k] * qk[k];
      }
      cosine = s_product / ( eucl_norm(nb_dim,u) * eucl_norm(nb_dim,qk) );
      res_to_stress_cos[j].index = rid;
      res_to_stress_cos[j].value = fabs(cosine);
      res_to_stress[j] = rid;
    }

    new_nb_res_to_stress = nb_res_to_stress;
    /* nb_adj_res_to_stress = new_nb_res_to_stress - nb_res_to_stress
     * which after the previous assignment must be, for the moment, equal to
     * zero */
    nb_adj_res_to_stress = 0;
    for (size_t j = 0 ; j < nb_res_to_stress ; ++j)
    {
      for (size_t k = 0 ; (k < adj[res_to_stress[j]][0]) ; ++k)
      {
        rid = adj[res_to_stress[j]][k+1];

        adj_res_found0 = lfind(&rid, res_to_stress, 
            &nb_res_to_stress, sizeof(size_t), compare_size_t);

        adj_res_found1 = lfind(&rid, adj_res_to_stress,
            &nb_adj_res_to_stress, sizeof(size_t), compare_size_t);

        if (adj_res_found0 == NULL && adj_res_found1 == NULL)
        {
          s_product = 0.0;
          for (size_t k = 0 ; k < nb_dim ; ++k)
          {
            u[k] = (svd->Ut)->value[k][rid];
            s_product += u[k] * qk[k];
          }
          cosine = s_product / ( eucl_norm(nb_dim,u) * eucl_norm(nb_dim,qk) );
          res_to_stress_cos[nb_res_to_stress + nb_adj_res_to_stress].index = 
            rid;
          res_to_stress_cos[nb_res_to_stress + nb_adj_res_to_stress].value = 
            fabs(cosine);
          adj_res_to_stress[nb_adj_res_to_stress] = rid;
          nb_adj_res_to_stress++;
          new_nb_res_to_stress++;
        }
      }
    }
    nb_res_to_stress = new_nb_res_to_stress;
    if (nb_res_to_stress > nb_res) 
      sentinel("nb_res_to_stress should never be greater than nb_res");

    idouble_sort(res_to_stress_cos, nb_res_to_stress);
    
    for (int m = 0 ; ; ++m)
    {
      for (size_t j = 0 ; j < nb_res ; ++j)
      {
        prev_res_deltas[j] = res_deltas[j];
      }
      for (size_t j = 0 ; j < nb_ter ; ++j)
      {
        prev_ter_deltas[j] = ter_deltas[j];
      }
      for (size_t j = 0 ; j < nb_res_to_stress ; ++j)
      {
        res_stress_factors[res_to_stress_cos[j].index] += STRESS_INC * (j+1);
      }
      for (size_t j = 0 ; j < nb_ter_to_stress ; ++j)
      {
        ter_stress_factors[ter_to_stress[j]] += STRESS_INC;
      }

      step ();

      for (size_t j = 0 ; j < nb_res ; ++j)
      {
        res_deltas_deltas[j] = res_deltas[j] - prev_res_deltas[j];
        sum_res_deltas[j] += res_deltas[j];
      }
      res_acceleration = eucl_norm(nb_res, res_deltas_deltas);
      if (res_acceleration < acceleration_threshold) 
      {
        debug("nb iterations before acceleration threshold: %d", m);
        break;
      }
    }

    nb_res_to_stress += RES_STRESS_QUANTUM;
    nb_ter_to_stress += TER_STRESS_QUANTUM;
    if (nb_res_to_stress > nb_res || nb_ter_to_stress > nb_ter) 
      break;
  }
  free(res_to_stress);
  free(ter_to_stress);
  free(adj_res_to_stress);
  free(prev_res_deltas);
  free(prev_ter_deltas);
  free(res_deltas_deltas);
  free(qk);
  free(u);
  free(res_to_stress_cos);
  /* Don't free sum_res_deltas and sum_res_deltas_s since they will be used 
   * until the end of the program */
  return;
error:
  exit(EXIT_FAILURE);
}

/* argv[1] should be the path to a text file that contains:
 * - (1) a line of resources' names separated by a space
 *   the index of a resource in this list will be used as an identifier for the
 *   resource
 * - (2) a line of terms' names sparated by a space
 *   the index of a term in this list will be used as an identifier for the
 *   term
 * - (3) a line indicating the size of the resources-terms matrix, 
 *   e.g. "132 X 2209" (this information is currently not used)
 * - (4) for each row of the matrix, a line of integers separated by 
 *   the space character
 *   these integers are the frequencies (i.e. nb of occurrences) of the terms 
 *   in the resources
 * - (5) a line of integers separated by a space character
 *   these integers are the identifiers of resources that are close to the query
 * - (6) a line of integers separated by a space character
 *   these integers are the identifiers of terms that are close to the query
 * - (7) for each resource, a line of integers separated by a space character,
 *   this line correspond to a row of the adjacency matrix of the graph obtained
 *   from the LOD
 * */
int 
main(int argc, char** argv)
{

  FILE* fp;
  char* line = NULL;
  size_t len = 0;
  ssize_t read;
  char sep = ' ';
  char* seps = " \n";
  int c; /* option character for getopt */
  int errflg = 0;
  int show_clock = 0;
  extern char* optarg;
  extern int optind, optopt;
  clock_t start_t, end_t, total_t;
  start_t = clock();

  /* d_line can contain nb_ter integers
   * it is used to build the matrix d 
   * for being able to use tokenize_size_t d_line is defined as an array of 
   * objects of type size_t but these objects are counters for the number 
   * of occurrences of a term in a resource (thus, they represent neither the 
   * size of a structure nor an index into a structure), therefore the type 
   * unsigned long would have been semantically more appropriate... */
  size_t* d_line;
  /* adj_line can contain nb_res integer
   * it is used to build the matrix adj */
  size_t* adj_line;
  /* nnz_adj_line is the number of non-zero value in adj_line */
  size_t nnz_adj_line;
  /* nz_adj_line is an array of size nb_res,
   * its first nnz_adj_line values are the identifiers of the resources
   * pointed by the resource corresponding of the current adj_line */
  size_t* nz_adj_line;

  while ((c = getopt(argc, argv, ":cs:a:")) != -1)
  {
    switch (c)
    {
      case 'c':
        show_clock++;
        break;
      case 's':
        sv_cut = atoi(optarg);
        break;
      case 'a':
        acceleration_threshold = atoi(optarg);
        break;
      case ':': /* -s or -a without operand */
        fprintf(stderr, "Option -%c requires an operand\n", optopt);
        errflg++;
        break;
      case '?':
        fprintf(stderr, "Unrecognized option: -%c\n", optopt);
        errflg++;
    }
  }

  if (optind != argc - 1)
  {
    fprintf(stderr, "wrong number of parameters\n");
    errflg++;
  }
  else
  {
    fp = fopen(argv[optind], "r");
    if (fp == NULL)
    {
      fprintf(stderr, 
              "ldsvd: failed to open the resources-terms matrix file\n");
      exit(EXIT_FAILURE);
    }
  }

  if (errflg)
  {
    fprintf(stderr, "usage: ldsvd [options] filepath\n\
-c: optional parameter, c stands for 'clock', when present the time\
spent by the algorithm is print out\n\n\
-s val: optional parameter, s stands for 'sv_cut', default value is 3,\
it represents the number of singular values that should be kept\n\n\
-a val: optional parameter, a stands for 'acceleration', default value\
is 300, it represents the acceleration threshold that commands the\
exit from the inner loop of the algorithm\n");
    exit(2);
  }

  /* (1) list of resources */
  if ((read = getline(&line, &len, fp)) != -1)
  {
    nb_res = count_char(line, sep) + 1;
    res = (char**) malloc(nb_res * sizeof(char*));
    check_mem(res);
    tokenize_str(line, seps, res);
  } 
  else 
  {
    fprintf(stderr, "ldsvd: failed to read the resources\n");
    exit(EXIT_FAILURE);
  }
  /* now that the number of resources is known, 
   * allocate res_deltas_s and res_deltas ; define nb_dim */
  res_deltas_s = (IDouble*) malloc(nb_res * sizeof(IDouble));
  check_mem(res_deltas_s);
  res_deltas = (double*) malloc(nb_res * sizeof(double));
  check_mem(res_deltas);
  nb_dim = min(sv_cut, nb_res);

  /* (2) list of terms */
  if ((read = getline(&line, &len, fp)) != -1)
  {
    nb_ter = count_char(line, sep) + 1;
    ter = (char**) malloc(nb_ter * sizeof(char*));
    check_mem(ter);
    tokenize_str(line, seps, ter);
  } 
  else 
  {
    fprintf(stderr, "ldsvd: failed to read the terms\n");
    exit(EXIT_FAILURE);
  }
  /* now that the number of terms is known, 
   * allocate ter_deltas_s and ter_deltas */
  ter_deltas_s = (IDouble*) malloc(nb_ter * sizeof(IDouble));
  check_mem(ter_deltas_s);
  ter_deltas = (double*) malloc(nb_ter * sizeof(double));
  check_mem(ter_deltas);

  /* (3) size of the resources-terms matrix */
  if ((read = getline(&line, &len, fp)) == -1)
  {
    fprintf(stderr, "ldsvd: failed to read the size of the matrix\n");
    exit(EXIT_FAILURE);
  }

  /* (4) resources-terms matrix line by line */
  d = svdNewDMat(nb_res, nb_ter);
  if (!d) 
  {
    fprintf(stderr, "ldsvd: failed to allocate d\n");
    exit(EXIT_FAILURE);
  }

  for (size_t i = 0 ; i < nb_res ; i++)
  {
    read = getline(&line, &len, fp);
    if (read == -1) 
    {
      fprintf(stderr, 
          "ldsvd: error while reading a line of the resources-terms matrix\n");
      exit(EXIT_FAILURE);
    }

    d_line = (size_t*) malloc(nb_ter * sizeof(size_t));
    check_mem(d_line);
    tokenize_size_t(line, seps, d_line);
    for (size_t j = 0 ; j < nb_ter ; j++)
    {
      d->value[i][j] = (double)d_line[j];
    }
    free(d_line);
  }
  
  /* (5) resources close to the query */
  if ((read = getline(&line, &len, fp)) != -1)
  {
    if (read == 1 && line[0] == 10) /* empty line: "\n" */
    {
      nb_res_close_to_query = 0;
    }
    else
    {
      nb_res_close_to_query = count_char(line,sep) + 1;
    }
    res_close_to_query = (size_t*) malloc(nb_res_close_to_query * 
                                          sizeof(size_t));
    check_mem(res_close_to_query);
    tokenize_size_t(line, seps, res_close_to_query);
  } 
  else 
  {
    fprintf(stderr, 
            "ldsvd: failed to read the resources close to the query\n");
    exit(EXIT_FAILURE);
  }
  
  /* (6) terms close to the query */
  if ((read = getline(&line, &len, fp)) != -1)
  {
    if (read == 1 && line[0] == 10) /* empty line: "\n" */
    {
      nb_ter_close_to_query = 0;
    }
    else
    {
      nb_ter_close_to_query = count_char(line,sep) + 1;
    }
    ter_close_to_query = (size_t*) malloc(nb_ter_close_to_query * 
                                          sizeof(size_t));
    check_mem(ter_close_to_query);
    tokenize_size_t(line, seps, ter_close_to_query);
  } 
  else 
  {
    fprintf(stderr, "ldsvd: failed to read the terms close to the query\n");
    exit(EXIT_FAILURE);
  } 

  /* (7) adjacency matrix line by line */
  adj = (size_t**) malloc(nb_res * sizeof(size_t*));
  check_mem(adj);
  for (size_t i = 0 ; i < nb_res ; i++)
  {
    read = getline(&line, &len, fp);
    if (read == -1) 
    {
      fprintf(stderr, 
              "ldsvd: error while reading a line of the adjacency matrix\n");
      exit(EXIT_FAILURE);
    }
    adj_line = (size_t*) malloc(nb_res * sizeof(size_t));
    check_mem(adj_line);
    nz_adj_line = (size_t*) malloc(nb_res * sizeof(size_t));
    check_mem(nz_adj_line);
    tokenize_size_t (line, seps, adj_line);
    nnz_adj_line = 0;
    for (size_t j = 0 ; j < nb_res ; j++)
    {
      if (adj_line[j] != 0)
      {
        nz_adj_line[nnz_adj_line] = j;
        nnz_adj_line++;
      }
    }
    adj[i] = (size_t*) malloc((nnz_adj_line + 1) * sizeof(size_t));
    check_mem(adj[i]);
    adj[i][0] = nnz_adj_line;
    for (size_t j = 0 ; j < nnz_adj_line ; j++)
    {
      adj[i][j+1] = nz_adj_line[j];
    }
    free(adj_line);
    free(nz_adj_line);
  }

  fclose(fp);

  /* first iteration */
  svd_norms(d);

  /* intialize res_stress_factors with the resources close to the query */
  res_stress_factors = (int*) malloc(nb_res * sizeof(int));
  check_mem(res_stress_factors);
  for (size_t i = 0 ; i < nb_res ; ++i)
  {
    res_stress_factors[i] = 1;
  }
  for (size_t i = 0 ; i < nb_res_close_to_query ; ++i)
  {
    res_stress_factors[res_close_to_query[i]] += 
      (INIT_STRESS_RES * STRESS_INC);
  }
  /* intialize ter_stress_factors with the terms close to the query */
  ter_stress_factors = (int*) malloc(nb_ter * sizeof(int));
  check_mem(ter_stress_factors);
  for (size_t i = 0 ; i < nb_ter ; ++i)
  {
    ter_stress_factors[i] = 1;
  }
  for (size_t i = 0 ; i < nb_ter_close_to_query ; ++i)
  {
    ter_stress_factors[ter_close_to_query[i]] += 
      (INIT_STRESS_TER * STRESS_INC);
  }

  step();

  /* next iterations */
  steps();

  
  if (show_clock)
  {
    end_t = clock();
    total_t = end_t - start_t;
    printf("%lu\n", total_t);
    printf("CLOCKS_PER_SEC = %ld\n", CLOCKS_PER_SEC);
  }
  

  /* print result */
  for (size_t j = 0 ; j < nb_res ; ++j)
  {
    sum_res_deltas_s[j].index = j;
    sum_res_deltas_s[j].value = sum_res_deltas[j];
  }
  idouble_sort(sum_res_deltas_s, nb_res);

  for (size_t j = 0 ; j < nb_res ; ++j)
  {
    printf("%s %f\n", res[sum_res_deltas_s[j].index], 
        sum_res_deltas_s[j].value);
  }

  exit(EXIT_SUCCESS);
error:
  exit(EXIT_FAILURE);
}
