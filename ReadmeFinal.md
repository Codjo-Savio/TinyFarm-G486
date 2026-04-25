1 - Aider les profs à se retrouver das le projet ( Arboraissance )
2 - Commandes à éxécutés pour faire fonctionner / tester le projet
3 - Automatisé l'apparission du Readme dès le lancement de l'éditeur de texte


### Projet : TinyFarm
# Made by Group 486


## Presentation of the project
==============================

TinyFarm is a website developed by students at the University of Nantes as part of the client-side web development course. It allows students to apply the concepts they have studied since the beginning of their Bachelor’s degree in Computer Science.

This project is loosely based on the principles of the game *My e-Farm*.

## Directory Structure
======================

TinyFarm directory : 
  | tinyfarm/backend  : ( includes all backend code.                                   )
  | database          : ( includes some of database needed such as tables, functions...)
  | frontend          : ( includes all frontend code.                                  )
  | reunions          : ( includes all markdown files describing all our meetups.      )
  | scratch           : ( others database functions.                                   )
  | screens           : ( include frontend prototype.                                  )
  |-------------------------------------------------------------------------------------
  
  
  Backend directory :
  | src/main/java/com/api/tinyfarm : ( includes all code directories ( Hibernate and needed files for Oath, Git login. ))
  | src/main/resouces              : ( includes files needed for GitHub authentification.                               )
  | src/test                       : ( includes all tests files.                                                        )
  | target                         : ( includes all maven files needs.                                                  )
  |----------------------------------------------------------------------------------------------------------------------
  
  src/main/java/com/api/tinyfarm directory :
  | controller : ( link backend classes and frontend.                 )
  | dto        : ()
  | model      : ( define all JPA classes.                            )
  | repository : ( define all functions signatures for JPA classes.   )
  | security   : ( includes all authentifications files.              )
  | service    : ( code all functions.                                )
  |--------------------------------------------------------------------
