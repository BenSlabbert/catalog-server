#!make

.PHONY: build
build: clean fmt
	mvnd install
	mvnd spotbugs:spotbugs
	docker buildx build . -t catalog-base:latest
	make -C item

.PHONY: fmt
fmt:
	mvnd spotless:apply

.PHONY: clean
clean:
	mvnd clean
