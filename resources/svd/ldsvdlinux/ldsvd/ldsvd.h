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

#ifndef LDSVD_H
#define LDSVD_H

#define min(a,b) ((a) < (b) ? (a) : (b))
#define max(a,b) ((a) < (b) ? (b) : (a))

/* indexed double struct */
typedef struct 
{
  size_t index; /* original position in the embedding structure */
  double value;
} IDouble;

/* in the resource-term matrix,
 * the lines corresponding to important resources,
 * and the columns corresponding to important terms,
 * are multiplied at each iteration by a factor equals to its value at the 
 * previous iteration + STRESS_INC
 * Before the first iteration, the stress factors are equal to 1 */
static int const STRESS_INC = 1;
/* at 1st iteration, the stress factor of up to RES_STRESS_QUANTUM resources 
 * (+ the resources linked to them in the graph),
 * and TER_STRESS_QUANTUM terms are increased.
 * at 2nd iteration, the stress factors of up to 2*RES_STRESS_QUANTUM resources 
 * etc. */
static int const RES_STRESS_QUANTUM = 1;
static int const TER_STRESS_QUANTUM = 10;
static int const INIT_STRESS_RES = 1000;
static int const INIT_STRESS_TER = 1000;

double 
eucl_norm (size_t n, double const * const v);

double* 
eucl_norm_cols (DMat const m);

DMat 
scale_cols (DMat const m, double const * const s);
DMat 
scale_rows (DMat const m, double const * const s);

void 
svd_norms (DMat const m);

void 
step ();
void 
steps ();

int 
compare_idouble_value (void const * a, void const * b);
void 
idouble_sort (IDouble* values, long size);
int 
compare_size_t (void const * a, void const * b);

#endif /* LDSVD_H */
