# My Flickr application

The Application provides an option to search for pictures in Flickr using a keyword.

The application provided  pictures list consists of all the pictures matching with the keyword.

For the Developer
The following description provides the detailed information on how the application is structured
1) MainActivity
   - Main Activity is the entry point for the Android Zygote system to the application
2) MVC architecture is followed in the development of My Flickr application
	Model 	- ImageRepostory Class handles all the data & model tasks
	View 	- UserInterfaceHandler in association with ViewController handles all the user interface tasks
	Control - FlickrHandler handles all the control tasks
3) Application view provides user with the Gallery view of the all matching thumnail images.
   By selecting any particular image in the Gallery fetches the selected image index and then renders the high resolution image of the selected thumbnail
	