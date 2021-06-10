import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class hexadecimal {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the bytes: "); //Here we take the size of the floating point data type as an int input
        int bytes = sc.nextInt();
        sc.nextLine();
        int bits = 8 * bytes;
        System.out.print("Enter the byte ordering (Little Endian , Big Endian): "); // We take the byte ordering type as a string input
        String byteOrder = sc.nextLine();
        byteOrder = byteOrder.toLowerCase();
        boolean le = false; //We are going to use this boolean variable to check whether the users wants to see little endian or big endian type
        if(byteOrder.compareTo("little endian") == 0) {
            le = true; //if the input is little endian then le is gonna be true
        }
        File file = new File("input.txt");
        sc = new Scanner(file); //We scan the input.txt here
        int a[] = new int[bits];
        while(sc.hasNext()) { //Unless we reach the end of the file this scanner keep scanning in this loop
            String temp = sc.nextLine(); //We scan the file line by line
            if(temp.contains("u")) { //If a line contains "u", that means we should use unsigned integer representation
                temp = temp.substring(0,temp.length()-1); //We subtract "u" from the line.
                a = positiveNumber(Integer.parseInt(temp),16); //With positiveNumber function that we defined below of main function we convert the number
                hexaPrint(a, 16, le); //hexaPrint function converts the number to correct byte ordering type and prints the output.
            }
            else if(!(temp.contains("-") || temp.contains("."))) {//If the line doesn't contain both ".","u" chars, then we will use 2’s complement representation
                a = positiveNumber(Integer.parseInt(temp),16); //Only positive numbers will been executed in this if statement, so we call our positiveNumber function
                hexaPrint(a, 16, le);
            }
            else if (temp.contains("-") && !temp.contains(".")) { //If the line is a negative number and doesn't contain "." this if statement occurs
                a = negativeNumber(Integer.parseInt(temp), 16);//We use our negativeNumber function which we defined below to convert these numbers
                hexaPrint(a, 16, le);
            }
            else {
                a = decimalNumber(Double.parseDouble(temp), bits); //Else statement occurs if the line is a floating point number
                hexaPrint(a, bits, le); //We use this function which we defined below to convert these numbers
            }
        }
        sc.close();

    }
    public static int[] positiveNumber(int number, int bits) { //This function converts positive numbers to binary representation
        int binaryNumber[] = new int[bits]; //We store the digits in this binaryNumber array
        for(int i = bits - 1; i >= 0; i--) {
            binaryNumber[i] = number % 2; //In this loop, we fill the array with correct digits by taking the remainder from the division of the number by 2
            number = number / 2;
        }
        return binaryNumber;
    }

    public static int[] negativeNumber(int number, int bits) {//This function converts negative numbers to binary representation
        int m;
        int binaryNumber[] = new int[bits];
        binaryNumber = positiveNumber((-1) * number, bits); //Firstly,we are taking the binary representation of positive value of this number
        for (m = bits - 1; m >= 0 ; m--) { //We convert the positive representation to negative with these operations
            if(binaryNumber[m] == 0) {
                binaryNumber[m] = 1;
            }
            else {
                binaryNumber[m] = 0;
            }
        }
        addOne(binaryNumber, bits);
        return binaryNumber;
    }

    public static int[] decimalNumber(double number, int bits) { //This function converts the floating point numbers
        int bitss,expBit;
        if(bits == 8){ //We assign that how many bits we are going to use for mantissa and exponent in these if statements according to the given info in pdf
            bitss = 5;
            expBit = 3;
        }
        else if(bits == 16) {
            bitss = 8;
            expBit = 8;
        }
        else if(bits == 24) {
            bitss = 14;
            expBit = 10;
        }
        else {
            bitss = 20;
            expBit = 12;
        }
        double precision = number % 1; //We store the right part of the "." in precision variable
        int integer = (int) number;//We store the left part of the "." in integer variable
        int integerNumber[] = new int[bits]; //We will store the digits in these 2 arrays
        int precisionNumber[] = new int[bits];
        int exp = 0, sign = 0;
        if(number < 0) { //If the number is negative we assign sign digit as 1 and we assign integer variable as -integer
            sign = 1;
            integer = -integer;
        }
        integerNumber = positiveNumber(integer, bitss); //Then we call positiveNumber function to convert the integer part with the given bits value
        int k = 0;
        int j = -1;
        while (precision > 0) { //In these loops we convert the precision part and store the digits in precisionNumber array
            double temp = Math.pow(2, j);
            if (precision >= temp) {
                precision -= temp;
                precisionNumber[k++] = 1;
            }
            else {
                precisionNumber[k++] = 0;
            }
            j--;
        }
        int copyIndex = 0;
        boolean zero = true;
        for (j = 0; j < integerNumber.length; j++) { //In this loop we take the first index of the integerNumber array that contains the digit 1
            if (integerNumber[j] == 1) {
                copyIndex = j; //We store that index in copyIndex variable
                zero = false; //This number is not zero since it contains a "1" so boolean zero is gonna be false
                break;
            }
        }
        if(!zero) { //In this if statement we remove the "0"s in front of the integer part
            k = copyIndex;
            exp = bitss - 1 - k;
            for (j = 0; j < bitss - k; j++, copyIndex++) {
                integerNumber[j] = integerNumber[copyIndex];
            }
            copyIndex = bitss - k;
            k = 0;
            for (j = copyIndex; j < bitss; j++, k++) {
                integerNumber[j] = precisionNumber[k];
            }
        }
        else { //This else statement occurs if the integer part is 0
            for(j = 0; j < integerNumber.length; j++) {//We directly assign precisionNumber to integerNumber since integer part is zero
                integerNumber[j] = precisionNumber[j];
            }
            for (j = 0; j < integerNumber.length; j++) { //Then we compute our exponent value
                if (integerNumber[j] == 1) {
                    copyIndex = j;
                    exp = -(j + 1);
                    break;
                }
            }
            k = copyIndex;
            for (j = 0; j < bitss - k; j++, copyIndex++) {
                integerNumber[j] = integerNumber[copyIndex];
            }
            k = copyIndex;
        }

        int m;
        if (precisionNumber[k] == 1) { //If the k index of remaining precisionNumber array, we round the number
            boolean control = false;
            for (j = k + 1; j < precisionNumber.length; j++) { //In this loop we search whether there is an another "1" in the remaining array or not
                if (precisionNumber[j] == 1) {
                    control = true; //If there is control variable is gonna be true
                }
            }
            if (control) { //If it is true we round the number by using our addOne function
                addOne(integerNumber, bitss);
            }
            else { //Also if it is not true but the index k-1 is a "1", we round the number
                if (precisionNumber[k - 1] == 1) {
                    addOne(integerNumber, bitss);
                }
            }
        }
        int [] b = new int[bits]; //In this b array we store the converted number
        b[0] = sign; //First index is going to be sign
        int bias1 = exp + (int)Math.pow(2, expBit - 1) - 1;
        int[] bias;
        bias = positiveNumber(bias1,expBit); //In this bias array we store the exponent part
        for(int i = 0; i < bias.length; i++){
            b[i + 1] += bias[i]; //We add the exponent part to final array
        }
        for(int i = 1; i < integerNumber.length; i++){
            b[bias.length + i] += integerNumber[i]; //And at the and we add the mantissa to final array
        }
        return b;
    }
    public static void hexaPrint(int [] number, int bits, boolean le) { //This function calculates hexadecimal value of the given number
        int [] hexaArray = new int[4];
        int m = 0;
        if(le) { //If le is true that means we use the Little Endian type
            for(int i = bits - 8; i >= 0; i -= 8) {
                for(int j = i; j < i + 8; j++) {
                    hexaArray[m++] = number[j];
                    if(j == i + 3 || j == i + 7) {
                        int hex = binaryToDecimal(hexaArray);
                        if(hex <= 9 && hex >= 0)
                            System.out.print(hex);
                        else
                            System.out.print((char)('A' + hex - 10));
                        m = 0;
                    }
                }
                System.out.print(" ");
            }
            System.out.println();
        }
        else { //If it is false then we convert it to Big Endian type
            for(int i = 0; i < number.length; i += 8) {
                for(int j = i; j < i + 8; j++) {
                    hexaArray[m++] = number[j];
                    if(j == i + 3 || j == i + 7) {
                        int hex = binaryToDecimal(hexaArray);
                        if(hex <= 9 && hex >= 0)
                            System.out.print(hex);
                        else
                            System.out.print((char)('A' + hex - 10));
                        m = 0;
                    }
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    public static int binaryToDecimal(int [] number) { //This function converts a given binary number to its decimal value
        int dec = 0;
        for(int i = 0; i < number.length; i++) {
            dec += number[i] * Math.pow(2, number.length - 1 - i);
        }
        return dec;
    }

    public static int[] addOne(int [] number, int bits) { //This function adds "1" to given binary number. We use that operation for rounding
        boolean extra = true;
        for(int i = bits - 1; i >=0; i--) {
            if(number[i] == 0) {
                number[i] = 1; 
                extra = false;
            }
            else if(number[i] == 1 && extra) {
                number[i] = 0;
            }
            else
                break;

        }
        return number;
    }
}
