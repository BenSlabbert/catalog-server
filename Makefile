#!make

M := "mvn"

.PHONY: build
build: clean fmt
	${M} install
	${M} spotbugs:spotbugs
	docker buildx build . -t catalog-base:latest
	make -C item

.PHONY: fmt
fmt:
	${M} spotless:apply

.PHONY: clean
clean:
	${M} clean
