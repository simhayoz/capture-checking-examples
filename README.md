# Capture checking examples

Collections of examples of the capture checking feature in Scala (see: [https://dotty.epfl.ch/docs/reference/experimental/cc.html](https://dotty.epfl.ch/docs/reference/experimental/cc.html))

## How to run

To run, first dotty should be built and published locally on the `cc-experiment` branch:

```console
git clone git@github.com:lampepfl/dotty.git
git checkout cc-experiment
sbt scala3-bootstrapped/publishLocal
```

This version of the compiler should be used in the `build.sbt` file.

## How to add a new example

To add a new example, run from this folder's root:

```console
sbt new scala/scala3.g8
```

Then the `build.sbt` should be modified the following way:
- The `scala3Version` to the version of the compiler that was published locally (see _How to run_).
- Add the following setting: `scalacOptions ++= Seq("-Ycc")`