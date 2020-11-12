BUILDDIR=./build/

all: | $(BUILDDIR)
	make -C src/

$(BUILDDIR):
	@echo "Creating $(BUILDDIR) directory for the binary output"
	@mkdir -p $(BUILDDIR)

clean:
	rm -f *~
	make clean -C src/
