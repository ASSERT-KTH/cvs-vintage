<!DOCTYPE style-sheet PUBLIC "-//James Clark//DTD DSSSL Style Sheet//EN" [
<!ENTITY dbstyle SYSTEM "/usr/share/sgml/docbook/stylesheet/dsssl/modular/html/docbook.dsl" CDATA DSSSL>
]>

<style-sheet>
<style-specification use="docbook">
<style-specification-body>

(define %html-ext% 
	;;HTML extension
	".html"
)

(define %root-filename%
	;; Name of the root file
	"index"
)

(define %stylesheet%
	;; Name and path of the CSS
	"carol.css"
)

(define %css-decoration%
	;; Enable use of CSS (use of CLASS element)
	#t)

(define %use-id-as-filename%
  ;; Use ID attributes as name for component HTML files?
  #t)

</style-specification-body>
</style-specification>
<external-specification id="docbook" document="dbstyle">
</style-sheet>