# hello-module

An Arachne module that provides a component which says hello when an Arachne
application starts.

This is designed to be a very simple module, but with all the basic parts, so
you can see how they work.

There are only a couple files to worry about:

- `resources/arachne-modules.edn` defines the module. It must be named exactly
`arachne-modules.edn`, on the root of the classpath, to allow the module to be
discovered and loaded.
- `src/arachne/hello.clj` contains all the source code for the module. Normally
modules would have their functionality split up between serveral source files,
but to make it easy to visualize this project has it all in one place.

These files are heavily commented and documented. Reading and understanding
them is highly recommended if you want to understand how modules work; all of
the code is very similar to what you will need to write, for any module.

## What it does

This module allows you to define components called "greeters." When Arachne
starts, each greeter will print out a custom message to `System/out`.

A config using this module looks something like this:

```clojure

(require '[arachne.core.dsl :as core])
(require '[arachne.hello :as hello])

(core/runtime :test/runtime [:test.greeting/spanish :test.greeting/informal])

(hello/greeter :test.greeting/spanish "Hola!")
(hello/greeter :test.greeting/informal "Hi!")
```

And the output when you start this Arachne app:
```
Hola!
Hi!
```

## How to run it

1. You can run the tests in `test/arachne/hello_test.clj`, which create a full
Arachne application using the module. There are two configs provided for trying
out the different possibilities, both in the `test-configs` directory.

2. You can compile the module into a jar in the standard way, using `lein
install` to install it to your local Maven repository. From there, you can
declare a dependency on it and use it from any Arachne project

Copyright © 2016 Luke VanderHart
