# Dwarves and Giants

    "If I have seen further, it is by standing on the shoulders of Giants." [1]    
    
## Giants

The Cinnamon ECMS is free open source software, so you can copy and use it in both
commercial and non-commercial settings under the very permissive LGPL 2.1 license.

But if you were to take a look at the source code, you would see that for each line 
of code we wrote ourselves, there are 100 or 1000 lines of code that we did not - and
which are absolutely essential to the working and function of Cinnamon. Those are the
libraries and frameworks that others have written and made available as open source for
us. Without their contribution, without those giants of software, who allow us to stand
on their shoulders, Cinnamon would simply not exist.

If you want to know exactly what libraries we use, have a look at the license page, which
tries to list all pieces of software that make up the Cinnamon server along with their
respective licenses. The largest pieces are listed here in no particular order (sorting giants
by alphabet is notoriously difficult, because each deems itself the most important). 
If I have missed a giant or giantess, it is because she is too far away or too close for this
little dwarf to see properly...

### Hibernate

Hibernate is the ORM framework which maps Java objects to database relations - and, although
this is really a hard problem, Hibernate does a great job doing so. It can be wired up to almost
every existing modern database system, so Cinnamon can be used with many different RDBMSs. 
Hibernate lives in the Red Hat mountains, on the lower ranges of the jBoss range.

### Java

While not 100% open source (just 99%), the Java ecosystem enables us to use a large number
of other free software projects and integrate their code into our system. 

### Grails and Groovy

Grails is the current platform for the web-client (Illicium) and the Cinnamon administration 
frontend (Dandelion). We are in the process of migrating the whole server over to Grails to
simplify server development and have all relevant server processes use one incredible framework 
with a lot of powerful and easily deployed plugins. Grails is sponsored by the emperor of virtualization,
which goes by the name of VMware, thus guaranteeing its further development.

### PostgreSQL

Postgres has become our relational database system of choice, since it is easy to deploy
and works on both Windows and Linux. It has good administration tools and is a joy to use.

### Linux

While Cinnamon server will run on any system that has a full Java runtime (Windows, Linux, probably
Mac), it is developed on Linux and feels most at home there. But one happy dwarf has installed it
on a Windows 2000 Server with MSSQL 2000, so even the ancient ones are not left behind in the 
darkness, where the bloated and monstrous legacy document management krakens dwell.

### Lucene

Apache Lucene is a library for indexing and searching documents. It is faster than lightning and
produces results with such a speed that we often wonder if we should add a couple of waiting seconds,
just so that users of proprietary content management system feel more at home. We use Lucene directly
so we can add dynamic custom index classes which allow us to selectively and semantically index 
complex documents.

### Tika

Apache Tika is a new member of the Giant tribe - and one of the bigger ones, as it's a giant
standing on the shoulders of giants... using many other open source libraries, Tika can read almost
every binary blob that it happens to come across. Extracting content from Excel, Word, OpenOffice
(and LibreOffice), extracting metadata from images and other media files - and all this can 
be used in combination with Lucene to make even difficult binary formats searchable.

### XML and XPath

    "XML is like violence. When in doubt, use more." (Anonymous Giant)
    
We use XML for most configuration files and for storing and handling custom metadata, without 
going overboard with needless complexity. The Cinnamon server responds to client requests with
easily parsed XML messages and you only need a little XPath to configure an indexer to allow
search for "all top level headlines in xhtml documents with only one paragraph of text below them".

### Chained Giants

Some giants still live in chains and are owned by large corporations. Still, if our customers
are happy with their services, we will of course try to climb those giants and have a look from
their shoulders, too.

* MS SQL Server
* .NET (C# and VB)

## Dwarves

Not all dwarves stay in the Cinnamon fortress, some venture out in search of greener pastures.
But still, they will always be remembered gratefully in the lists of contributors:

### Dwarves of old

* Stefan Rother - client and server development
* Dirk Ufermann - client development
* Rainer Materna - client development

### Modern Dwarves

* Boris Horner - project management, client development (original author of Cinnamon)
* Webdesign
    * Mediartists - [cinnamon-cms.de](http://cinnamon-cms.de)
    * Web client - Michèle Horner
* Ingo Wiarda - project management, administration, server development, documentation

## A Dwarf's toolkit

* IntelliJ IDEA
* Visual Studio
* Notepad++
* PsPad
* Emacs
* Virtualization: KVM, VMware
* Subversion + Sourceforge repository
* Git + Github
* Firefox
    * Firebug
    * Web Developer's Toolbar
* Linux CLI
* many, many others



[1]: [Isaac Newton](http://en.wikipedia.org/wiki/Standing_on_the_shoulders_of_giants)

<!-- Author: Ingo Wiarda 2012 -->
