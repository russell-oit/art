# ART messages
#
# Enables the ART user interface to be translated into different languages
# The default language file, ArtMessages.properties, is in UK English.
#
# Message key format is <page>.<context>.<id>
#
# To create a new translation,
# - Copy and rename the ArtMessages.properties file to ArtMessages_xx.properties where xx is the 
# ISO code for the new language(e.g. ArtMessages_de.properties for German)
# - Translate the text after the = signs
# If you used any non ISO-8859 characters, use native2ascii tool to convert the file e.g
# native2ascii -encoding utf-8 ArtMessages_de.properties ArtMessages_de.properties
# - Email the new translation file to tanyona@users.sf.net
#
# To test for yourself, 
# - Copy the new file to WEB-INF/i18n/
# - Log in to ART
# - Type a url like the following in the browser, http://localhost:8080/art/reports?lang=xx
# - The application should be displayed in the new language for the remainder of the session
# - You can also modify the languages.properties file to add the new language
# to the languages list displayed by the application.
#
# A comment line begins with the # char

Online translation
ART has a translation project on the Crowdin platform. You can create an account on Crowdin and
join the ART translation project at the following link. https://crowdin.com/project/artreporting
You can then translate online. If your language is not available, create a post on the ART sourceforge
forum requesting for the new language to be added to the Crowdin project.

Translators/Credits
https://sourceforge.net/p/art/wiki/Translators/
