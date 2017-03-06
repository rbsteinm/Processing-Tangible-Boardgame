# Processing Boardgame
##### Author: Raphaël Steinmann
\
This game was coded in the scope of the course "[Introduction to Visual Computing](http://edu.epfl.ch/coursebook/en/introduction-to-visual-computing-CS-211?cb_cycle=bama_cyclemaster&cb_section=sv_b)" (prof. Pierre Dillenbourg, CHILI Lab, EPFL). This is a simple tilt board game where the user accumulates points by hitting the pins with the ball.

## Digital and tangible interractions
There are two different ways to interract whith the game:
- Digital: the player tilts the board by clicking and dragging the mouse
- Tangible: for this mode the user needs a webcam and a square green lego board. The player tilts the digital board by tilting the lego board. Via some image processing on the frame captured by the camera, the program detects the position and the inclination angle of the board. Go to "Tangible demo" mode to see a demo with a pre-recorded video.

## Edit mode
By holding the SHIFT key the user enters the edit mode in which the game pauses and he can add more pins on the board by clicking on the desired location. The user must also add at least one pin before beginnig a new game.

## Setup
- This game was coded with Processing 2. To run the code you must first follow [this tutorial](https://processing.org/tutorials/eclipse/) to use the Processing Library in Eclipse.
- Background music and hit sound animation were added, but commented. If you want to use it, import ddf.minim.* and uncomment concerned lines (declarations, setup(), audioSetup(), removeHitPins()).