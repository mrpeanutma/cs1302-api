# Deadline

Modify this file to satisfy a submission requirement related to the project
deadline. Please keep this file organized using Markdown. If you click on
this file in your GitHub repository website, then you will see that the
Markdown is transformed into nice looking HTML.

## Part 1: App Description

> Please provide a firendly description of your app, including the
> the primary functions available to users of the app. Be sure to
> describe exactly what APIs you are using and how they are connected
> in a meaningful way.

> **Also, include the GitHub `https` URL to your repository.**

My app retrieves the best deals for a specified game keyword or under a
certain price point. From the GameSharkAPI, the program retrieves the game
title, the thumbnail, the link to the best deal, and the Steam App ID.
The program then uses that App ID to retrieve statistics on the game from
the BarterVG API, such as the player count and recent player, to give a more
holistic view of the game. Once this search is complete, the game then displays


## Part 2: New

> What is something new and/or exciting that you learned from working
> on this project?

I learned that POST requests are much more complicated than GET requests, and how to create
arrays of objects when parsing JSON responses, rather than an object of arrays.

## Part 3: Retrospect

> If you could start the project over from scratch, what do
> you think might do differently and why?

I think I would display the results differently. I wanted to make it so that the thumbnails
are displayed and you can click on a thumbnail to see the game, but I ran out of time before
I could modify the code. I also would include more parameters to display to the user and be
wary of the duplicate responses.
