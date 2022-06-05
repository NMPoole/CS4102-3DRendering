# CS4102-3DRendering

This repository demonstrates work completed as part of the **CS4102 Computer Graphics** module for Practical 2. 
The aim of this practical was to understand key principles behind various techniques frequently used for the rendering of 3D objects, 
and give hands-on experience with their implementation and manipulation. The task involved the creation of **an application which facilitates 
interactive modelling of faces in 3D**.

The application takes 3D coordinates from input files (omitted due to their size) which together form the mesh of a person's face using connected 
triangle polygons. Many such face meshes can be loaded and the user is given the ability to render 3D faces which are an interpolation of various faces 
taken in combination. Rendering uses the Painter's algorithm to create an orthographic view of the generated 3D face. Different lighting models can be applied to said face, 
such as flat shading or interpolation (i.e., Gouraud) shading. The generated 3D face can also be manipulated via rotation.
