CC=gcc -Wall -std=gnu99
CFLAGS=-I.
LIBS=-lm -lsvd
LIBSPATHS=-L.
DEPS=svdlib.h
OBJ=ldsvd.o str.o

all: CC += -O3 -DNDEBUG
all: ldsvd

debug: CFLAGS += -DDEBUG -g
debug: CC += -O0
debug: ldsvd

ldsvd: ${OBJ}
	${CC} ${OBJ} ${CFLAGS} ${LIBSPATHS} ${LIBS} -o $@

%.o: %.c %.h ${DEPS}
	${CC} -c -o $@ $< ${CFLAGS}

.PHONY: clean

clean:
	rm *.o
