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

#ifndef STR_H
#define STR_H

size_t 
count_char (char const * const str, char c);

void 
tokenize_str (char const * const str, char const * const seps, char** array);

void 
tokenize_size_t (char const * const str, char const * const seps, 
                 size_t* array);

#endif /* STR_H */
