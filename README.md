Spectacle
=========

[ ![Download Nightly](https://api.bintray.com/packages/gauge/spectacle/Nightly/images/download.svg) ](https://bintray.com/gauge/spectacle/Nightly/_latestVersion)
[![Build Status](https://app.snap-ci.com/getgauge/spectacle/branch/master/build_image)](https://app.snap-ci.com/getgauge/spectacle/branch/master)
[![Build Status](https://travis-ci.org/getgauge/spectacle.svg?branch=master)](https://travis-ci.org/getgauge/spectacle)

Generates HTML from Specification/Markdown files. This is a plugin for [gauge](http://getgauge.io).

Install through Gauge
---------------------
```
gauge --install spectacle
```

Export to HTML
--------------
Run the following command to export to HTML in a Gauge project

```
gauge --docs spectacle <path to specs dir>
```

Build from Source
-----------------

### Requirements
* [Golang](http://golang.org/)

### Compiling

```
go run build/make.go
```

For cross-platform compilation

```
go run build/make.go --all-platforms
```

### Installing
After compilation

```
go run build/make.go --install
```

### Creating distributable

Note: Run after compiling

```
go run build/make.go --distro
```

For distributable across platforms: Windows and Linux for both x86 and x86_64

```
go run build/make.go --distro --all-platforms
```
License
-------

![GNU Public License version 3.0](http://www.gnu.org/graphics/gplv3-127x51.png)
`Spectacle` is released under [GNU Public License version 3.0](http://www.gnu.org/licenses/gpl-3.0.txt)
