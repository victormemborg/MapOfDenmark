
# Map of Denmark - Group 20
**Made by:** Kasper Jønsson (kasjo), Peter Hansen (pbjh), Philip Rosenhagen (phro), and Victor Memborg (vmem)

This is a Java application that allows parsing and viewing OpenStreetMap (OSM) files. The application enables users to mark and save different points on the map, which can be customized with names and colors. Furthermore, users can search for addresses, which will be autocompleted as they type. The user can provide two addresses, and the program will find the quickest route between these two points with a chosen transport option. The program comes with a debug window that can be used to show the inner workings of the viewport and the heuristics of the pathfinding algorithm. A binary file is embedded into the application if the user quickly wants to load up a map of Denmark. Users can also provide a custom "\*.osm" file containing an arbitrary area **within Denmark**. This OSM file can also be loaded from a "\*.bz2" (zipped) file.

## To run the program use:

```
java -Xms4g -Xmx10g -jar MapOfDenmark-1.0-SNAPSHOT.jar
```

When the program has opened, you can choose to load the default binary file from the File item of the menubar (excluded in the repo). Here, you can also choose to parse an OSM file or load an already parsed binary file.

## Please note:

- This program must be run with java 19.0.2 https://github.com/corretto/corretto-19/releases
- This program only works within the bounds of Denmark (excluding Greenland and the Faroe Islands).
- For parsing, you should expect to use 10GB of memory.
- After loading a binary file, the program will use at most approximately 5GB of memory.
- All binary files must be parsed by this program.
