![alt INSTALLER banner](doc/images/viper-wide-banner.jpg)

# Installer (In Progress)

[Installer](http://www.tnevin.com/software/installation.html)

Installer is an UI application which allows one to install other software on Windows orLinux systems.

## Features

The Installer command line tool has the following features:
 

## Additional Documentation

In progress: 
* [Authors Home Page](http://www.tnevin.com)

## Getting Started
 
### Prerequisites

What things you need to install the software and how to install them

```
* java 1.6 or better.
* ant 1.9 or better (optional).
* For Windows, install CygWin, latest.
```

Note: ant commands have been run and tested using cygwin bash shell, dos shell, and other linux shells will probably work.

### Installing

1. Download the installer zip file, and unzip it.

```
https://github.com/vipersoftwareservices/installer
```

2. Run the build script if building sources is desired, runtime jars are available.

```
ant clean all
```


## Running the tests

Run the tests, by running ant command.

```
ant test
```

View the JUnit test results, by bringing the following file up in browser.
For windows, double click the file in the disk explorer, the location of the file is:

```
<install-directory>/build/reports/index.html
```

View the code coverage file in the browser..
For windows, double click the file in the disk explorer, the location of the file is:

```
<install-directory>/build/jacoco/index.html
```

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Check on coding style by running:

```
ant checkstyle
```

## Usage

In order to use the installation tool, you will need to create and edit some files:
```
installation.xml, required, an xml file of configuration data which will be used to fill the wizard pages of the UI for doing the installation.
license.html, optional, an html file for displaying the license for your application which must be accepted during the installation file
readme.txt, optional, an html/txt file which is to be displayed after the installation has occurred.
shortcut.<cmd sh>, optional, a set of scripts which will run on differenet OS's for to install a shortcut and desktop icon for your application, typically used for client installations.
<wizardside.gif>, optional, an image to display as the background on the installation wizard.
<application.jar>, required, the file or files which are to be installed.     
```

The heart of your installation setup is the installation.xml file, following is a small portion of the installation file, see the sources for complete sample.

TODO not done yet  

## Built With
 
## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/vipersoftwareservices/installer) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

In progress

## Authors

* **Tom Nevin** - *Initial work* - [Installer](https://github.com/vipersoftwareservices/installer)

See also the list of [contributors](https://github.com/vipersoftwareservices/installer/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details
 

