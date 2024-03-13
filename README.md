# Assignment 01 - Spam Detector 

## Project Overview
Spam Detector is a program we designed to filter our spam emails using a unigram approach. The program reads a dataset of emails(spam and non-spam) to train and recognize whether new emails are spam. It calculates probabilities based on the frequency of words in the emails. The project includes two main componets: a server-side application for training and testing the model, and a web client application to display the results.

**Group Members:**
Jeremy Thummel,
Ryan Hastings,
Myron Lobo

## Improvements
Some improvements we made to the interface and/or the model are:

• We enhanced user interface for better user experience

• We imporved algorithm efficiency for faster processing

## How to Run the Program
To run the program follow these steps below:

Firstly, clone  the repo by using `git clone`

Secondly, after opening the model in intelliJ
 make sure that your run configurations are all set correctly. Make sure you have created a Glass Fish server with the end point being `http://localhost:8080/spamDetector-1.0/api/spam`. Then, make sure that your artifact is deployed. Now you will be able to run the glass fish server.

Lastly, now with the server running the you can now run the html file which will allow you to traverse through the program.

Now you can see a table of different emails being classifed as spam and ham and the spam probabilities. You can also find the probability and accuracy values of our model as well.




