# default-db-format
Checks that your Om Next db is in the 'default db format'

#### Hacking
The source code that makes it into the library jar file is all in one `.cljs` file
that only depends on cljs.pprint.

However you can interact with the examples from a web page.
The examples are all `defs` in ...



##### See example devcards in a browser using boot

`boot dev`  
Set browser to `http://localhost:3000` and open Chrome console   
Change the source in the `default-db-format.cards` namespace  
You may need to refresh the browser to see the changes  

##### ~~Install as a jar in local Maven repo~~
boot pom jar install

##### ~~Start a REPL to check one of the example dbs~~  
boot repl -c  
(start-repl)  
(in-ns 'default-db-format)   
So far so good but at the moment it just crashes, so don't bother with this!


