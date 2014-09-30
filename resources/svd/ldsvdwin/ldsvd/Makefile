CC=gcc -std=c99 -Wall -O3
CFLAGS=-I.
LIBS=-lm -lsvd
LIBSPATHS=-L.
DEPS=svdlib.h
OBJ=ldsvd.o

ldsvd: ${OBJ}
	${CC} ${OBJ} ${CFLAGS} ${LIBSPATHS} ${LIBS} -o $@

%.o: %.c ${DEPS}
	${CC} -c -o $@ $< ${CFLAGS}

.PHONY: clean

clean:
	rm *.o
