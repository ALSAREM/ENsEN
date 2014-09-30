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
#include "dbg.h"
#include "str.h"

/* count_char counts how many times the character c appears in the string str 
 * but not in last position */
size_t 
count_char (char const * const str, char c)
{
  char const * p = str;
  size_t count = 0;

  do 
  {
    if (*p == c) count++;
  } 
  while (*(++p));

  if (*(p - 2) == c) 
    count--;

  return count;
}

/* tokenize_str fills array with the tokens found in str and separated by one 
 * of the characters of seps.
 * array must have been allocated enough memory.
 * the space necessary for array can be found with a call to count_char. */
void 
tokenize_str (char const * const str, char const * const seps, 
              char** array)
{
    char* tokenstring = malloc(strlen(str) + 1);
    check_mem(tokenstring);
    char* token;
    int i = 0;
    strcpy(tokenstring, str);
    token = strtok(tokenstring, seps);

    while (token != NULL)
    {
      array[i] = malloc(strlen(token) + 1);
      check_mem(array[i]);
      strcpy(array[i], token);
      token = strtok(NULL, seps);
      i++;
    }
    free(tokenstring);
    return;

error:
    exit(EXIT_FAILURE);
}

/* tokenize_size_t fills array with the integer found in str and separated by 
 * one of the characters of seps.
 * These integers are interpreted as indices of a data structure.
 * Thus they are of type size_t.
 * array must have been allocated enough memory. */
void 
tokenize_size_t (char const * const str, char const * const seps, 
                 size_t* array)
{
    char* tokenstring = malloc(strlen(str) + 1);
    check_mem(tokenstring);
    char* token;
    size_t i = 0;
    size_t value;
    strcpy(tokenstring, str);
    token = strtok(tokenstring, seps);

    while (token != NULL)
    {
      sscanf(token, "%zu", &value);
      array[i] = value;
      token = strtok(NULL, seps);
      i++;
    }
    free(tokenstring);
    return;

error:
    exit(EXIT_FAILURE);
}
