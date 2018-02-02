package badgegenerator.util;

/**
 * Class is used for vatious helper functions, like replace camel case or...
 */
public class Util {
    public static String retrieveWordsFromCamelCase(String camelCasedWord) {
        StringBuilder words = new StringBuilder();
        int capitalLetterIndex = 0;
        char[] charArray = camelCasedWord.toCharArray();
        for (int i = 1; i < charArray.length; i++) {
            char letter = charArray[i];
            if (Character.isUpperCase(letter)) {
                if (words.length() == 0) words.append(camelCasedWord.substring(0, i));
                else words.append(" ")
                        .append(camelCasedWord.substring(capitalLetterIndex, i));

                capitalLetterIndex = i;
            }
        }
        if (words.length() > 0) words.append(" ");
        words.append(camelCasedWord.substring(capitalLetterIndex, camelCasedWord.length()));
        camelCasedWord = words.toString();
        return camelCasedWord;
    }
}
