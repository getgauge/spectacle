Spectacle
=========

[ ![Download Nightly](https://api.bintray.com/packages/gauge/spectacle/Nightly/images/download.svg) ](https://bintray.com/gauge/spectacle/Nightly/_latestVersion)
[![Build Status](https://app.snap-ci.com/getgauge/spectacle/branch/master/build_image)](https://app.snap-ci.com/getgauge/spectacle/branch/master)
[![Build Status](https://travis-ci.org/getgauge/spectacle.svg?branch=master)](https://travis-ci.org/getgauge/spectacle)

Generates HTML from Specification/Markdown files. This is a plugin for [gauge](http://getgauge.io).

<img src="https://github.com/getgauge/spectacle/raw/master/images/spectacle.png" alt="Spectacle" style="width: 600px;"/>


Installation
------------

```
gauge install spectacle
```
To install a specific version of spectacle plugin use the ``--version`` flag.

```
gauge install spectacle --version $VERSION
```

### Offline Installation

Download the plugin zip from the [Github Releases](https://github.com/getgauge/spectacle/releases).

use the ``--file`` or ``-f`` flag to install the plugin from  zip file.

```
gauge install spectacle --file ZIP_FILE_PATH
```



### Build from Source

#### Requirements
* [Golang](http://golang.org/)

#### Compiling

```
go run build/make.go
```

For cross-platform compilation

```
go run build/make.go --all-platforms
```

#### Installing
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

Usage
-----

**Export to HTML**

Run the following command to export to HTML in a Gauge project

```
gauge docs spectacle <path to specs dir>
```

The html docs are generated in `docs` directory inside the project.

**Filter Specification/Scenario based on Tags**

Tags allow you to filter the specs and scenarios. Add the tags to the
textbox in the report to view all the specs and scenarios which are
labeled with certain tags. Tag expressions with operators ``|``, ``&``,
``!`` are supported.

In the following image, the specs/scenarios are filtered using a tag expression(\ ``refactoring & !api``).

<img src="https://github.com/getgauge/spectacle/raw/master/images/filter_tags.png" alt="filter" style="width: 600px;"/>



License
-------

![GNU Public License version 3.0](http://www.gnu.org/graphics/gplv3-127x51.png)
`Spectacle` is released under [GNU Public License version 3.0](http://www.gnu.org/licenses/gpl-3.0.txt)
