# Author:  Vadim Nasardinov (vadimn@redhat.com)
# Version: $Id: Makefile,v 1.2 2005/02/15 14:07:46 benoitf Exp $
# Since:   2005-02-07

build:=../output
in_dir := usermanual
out_dir := $(build)/$(in_dir)

db_files := $(shell find $(in_dir) -name *.docbook)
img_files_in := $(wildcard $(in_dir)/img/*.*)
img_files_out := $(addprefix $(build)/,$(img_files_in))

.PHONY : all docbook-html docbook-pdf images clean

all: docbook-html docbook-pdf

clean:
	rm -rf $(out_dir)

style:
	cp $(in_dir)/carol.css $(out_dir)/


docbook-html: images style $(out_dir)/book1.html

docbook-pdf: $(out_dir)/usermanual.pdf

$(out_dir)/usermanual.pdf: $(db_files)
	docbook2pdf --output $(out_dir) usermanual/usermanual.docbook



images: $(img_files_out)

$(out_dir)/img/%: $(in_dir)/img/%
	@mkdir -p $(@D)
	cp $< $@

$(out_dir)/book1.html: $(db_files)
	mkdir -p $(@D)
	docbook2html --dsl usermanual/style.dsl --output $(out_dir) usermanual/usermanual.docbook

