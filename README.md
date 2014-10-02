BMaD
====

A Boolean Matrix Decomposition Framework


Boolean matrix decomposition is a method to obtain a compressed
representation of a matrix with Boolean entries. BMaD is a modular
framework that unifies several Boolean matrix decomposition
algorithms, and provide methods to evaluate their performance. The
main advantages of the framework are its modular approach and hence
the flexible combination of the steps of a Boolean matrix
decomposition and the capability of handling missing values. The
framework is licensed under the GPLv3.  


Build
=====

We use maven as a build tool, so just run

```
mvn clean install
```

to build BMaD. 

You can include BMaD using our maven repository:

```
<repository>
	<id>jgu</id>
	<name>jgu</name>
	<url>http://fantomas.informatik.uni-mainz.de/mvnrepo/repository</url>
</repository>
```
```
<dependency>
	<groupId>org.kramerlab</groupId>
	<artifactId>bmad</artifactId>
	<version>2.4</version>
</dependency>
```


Citation
========

If you want to cite BMaD in your publication, please cite the
following ECML/PKDD paper: 

```
Tyukin, Andrey, Stefan Kramer, and Jörg Wicker. 
"BMaD–A Boolean Matrix Decomposition Framework." 
Machine Learning and Knowledge Discovery in Databases. 
Springer Berlin Heidelberg, 2014. 481-484.
```
Bibtex entry:

```
@incollection{
year={2014},
isbn={978-3-662-44844-1},
booktitle={Machine Learning and Knowledge Discovery in Databases},
volume={8726},
series={Lecture Notes in Computer Science},
editor={Calders, Toon and Esposito, Floriana and H{\"u}llermeier, Eyke and Meo, Rosa},
doi={10.1007/978-3-662-44845-8_40},
title={BMaD – A Boolean Matrix Decomposition Framework},
url={http://dx.doi.org/10.1007/978-3-662-44845-8_40},
publisher={Springer Berlin Heidelberg},
author={Tyukin, Andrey and Kramer, Stefan and Wicker, J{\"o}rg},
pages={481-484},
language={English}
}
```

