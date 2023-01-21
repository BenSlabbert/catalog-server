#!make

.PHONY: build
build: clean fmt
	mvnd install
	docker buildx build . -t catalog

.PHONY: fmt
fmt:
	mvnd spotless:apply

.PHONY: clean
clean:
	mvnd clean
