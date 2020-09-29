package nstic.util

/**
 * Created by brad on 3/25/14.
 */
class PasswordUtil {

    private static Random RAND = new Random(System.currentTimeMillis());
    private static List<String> PASSWORD_ALPHABET =
            ["0123456789", "abcdefghikjmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", "!@#%^&*"];

    static String generateRandom(){
        return generateRandom(6);
    }
    static String generateRandom(int length){
        List<Character> passwordBuffer = []
        if( length < 6 ){
            throw new UnsupportedOperationException("Password too short, must be greater than or equal to 6.")
        }
        // Add 2 from each character class.
        for( int charClassIndex = 0; charClassIndex < PASSWORD_ALPHABET.size(); charClassIndex++ ){
            passwordBuffer.add(PASSWORD_ALPHABET[charClassIndex].charAt(RAND.nextInt(PASSWORD_ALPHABET[charClassIndex].length())));
            passwordBuffer.add(PASSWORD_ALPHABET[charClassIndex].charAt(RAND.nextInt(PASSWORD_ALPHABET[charClassIndex].length())));
        }

        while( passwordBuffer.size() < length ){
            int charClassIndex = RAND.nextInt(10);
            // Statistically will pick more from the alphabet for ease to the user.
            if( charClassIndex <= 5 ){
                charClassIndex = 1;
            }else if( charClassIndex <= 8 ){
                charClassIndex = 0;
            }else{
                charClassIndex = 2;
            }
            passwordBuffer.add(PASSWORD_ALPHABET[charClassIndex].charAt(RAND.nextInt(PASSWORD_ALPHABET[charClassIndex].length())));
        }

        Collections.shuffle(passwordBuffer);

        StringBuilder builder = new StringBuilder();
        passwordBuffer.each{ c ->
            builder.append(c.charValue());
        }
        return builder.toString();
    }

    static String isValid( String password ){
        if( password.length() < 6 ) {
            return "password.too.short"
        }

        int numberChars = 0;
        int letterChars = 0;
        int specialChars = 0;
        for( int i = 0; i < password.length(); i++ ){
            Character c = password.charAt(i);
            if( Character.isDigit(c) ){
                numberChars++;
            }else if( Character.isLetter(c) ){
                letterChars++;
            }else{
                specialChars++;
            }
        }

        if( numberChars < 2 ){
            return "password.must.contain.numbers";
        }else if( letterChars < 2 ){
            return "password.must.contain.letters";
        }

        return null;
    }


    static Boolean hasTwoNumbers( String str ){
        int count = 0;
        for( int i = 0; i < str.length(); i++ ){
            if( Character.isDigit(str.charAt(i)) )
                count++;
        }
        return (count >= 2);
    }

    static Boolean hasTwoLetters( String str ){
        int count = 0;
        for( int i = 0; i < str.length(); i++ ){
            if( Character.isLetter(str.charAt(i)) )
                count++;
        }
        return (count >= 2);
    }

}
