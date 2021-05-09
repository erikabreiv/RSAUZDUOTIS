import java.math.BigInteger;

import java.util.Random;

import java.io.*;



public class RSA {


    private static final String PUBLIC_KEY_FILE = "ViesasisRaktas.txt";
    private static final String RESULT_FILE = "Result.txt";


    public static void main(String[] args) throws IOException {

        // Priskiriamos p ir q reikšmės
        BigInteger p = largePrime(512);
        BigInteger q = largePrime(512);

        // apskaičiuojame n iš p ir q
        // n yra mod privačiajam ir viešajam raktams, n bitų ilgis yra lygus rakto ilgiui
        BigInteger n = n(p, q);

        // Apskaičiuojame Phi(n) (Eulerio koeficiento funkcija)
        // Phi(n) = (p-1)(q-1)
        // BigInteger yra objektai ir turi naudoti algebrines operacijas
        BigInteger phi = getPhi(p, q);

        // Apskaičiuoti int e taip, kad 1 <e <Phi (n) ir gcd (e, Phi) = 1
        BigInteger e = genE(phi);


        //Apskaičiuojam d pagal formulę  d ≡ e^(-1) (mod Phi(n))
        BigInteger d = extEuclid(e, phi)[1];

        // Atspausdiname apskaičiuotas reikšmes
        System.out.println("p: " + p);
        System.out.println("q: " + q);
        System.out.println("n: " + n);
        System.out.println("Phi: " + phi);
        System.out.println("e: " + e);
        System.out.println("d: " + d);


        // Žinutė, kurią koduojame
        String x = "Labas mano vardas yra Erika";
        // Konvertuojame string į numerį
        BigInteger konvert = stringCipher(x);
        // Užkodavimas
        BigInteger encrypted = encrypt(konvert, e, n);
        // Atkodavimas
        BigInteger decrypted = decrypt(encrypted, d, n);
        //Iššifruotą pranešimą iššifruokite į tekstą
        String y = cipherToString(decrypted);

        System.out.println("X: " + x);
        System.out.println("Užšifruotas RSA: " + encrypted);
        System.out.println("Dešifruotas RSA: " + decrypted);
        System.out.println("Y: " + y);

        //Įrašomas e į failą, nes, tai yra viešasis raktas
        //Įrašomas gautas užšifruotas tekstas į failą 
        RSA obj = new RSA();

        obj.toFile(PUBLIC_KEY_FILE, e);
        obj.result(RESULT_FILE, encrypted);


    }

    //Paima eilutę ir konvertuoja kiekvieną simbolį į ASCII dešimtainę vertę
    //Grąžina BigInteger
    public static BigInteger stringCipher(String message) {
        message = message.toUpperCase();
        String cipherString = "";
        int i = 0;
        while (i < message.length()) {
            int ch = (int) message.charAt(i);
            cipherString = cipherString + ch;
            i++;
        }
        BigInteger cipherBig = new BigInteger(String.valueOf(cipherString));
        return cipherBig;
    }


    //Paima šifruotą „BigInteger“ ir paverčia jį atgal į paprastą tekstą
    //Grąžina String
    public static String cipherToString(BigInteger message) {
        String cipherString = message.toString();
        String output = "";
        int i = 0;
        while (i < cipherString.length()) {
            int temp = Integer.parseInt(cipherString.substring(i, i + 2));
            char ch = (char) temp;
            output = output + ch;
            i = i + 2;
        }
        return output;
    }


    //Apskaičiuokite Phi (n) (Eulerio koeficiento funkcija) Phi (n) = (p-1) (q-1)
    public static BigInteger getPhi(BigInteger p, BigInteger q) {
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        return phi;
    }

    //Generuoja atsitiktinai didelį nurodytą bitų ilgio pirminį skaičių
    public static BigInteger largePrime(int bits) {
        Random randomInteger = new Random();
        BigInteger largePrime = BigInteger.probablePrime(bits, randomInteger);
        return largePrime;
    }


   // Rekursyvus Euklido algoritmo įgyvendinimas siekiant rasti didžiausią bendrą vardiklį
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) {
            return a;
        } else {
            return gcd(b, a.mod(b));
        }
    }


    // Rekursinis IŠPLĖSTAS Euklido algoritmas, išsprendžia Bezouto tapatybę (ax + by = gcd (a, b))
    // Suranda dauginamąją atvirkštinę, kuri yra  ax ≡ 1 (mod m) sprendinys
    // Grąžina [d, p, q], kur d = gcd (a, b) ir ap + bq = d

    public static BigInteger[] extEuclid(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) return new BigInteger[]{
                a, BigInteger.ONE, BigInteger.ZERO
        }; // { a, 1, 0 }
        BigInteger[] vals = extEuclid(b, a.mod(b));
        BigInteger d = vals[0];
        BigInteger p = vals[2];
        BigInteger q = vals[1].subtract(a.divide(b).multiply(vals[2]));
        return new BigInteger[]{
                d, p, q
        };
    }



     //Sugeneruojamas e, viešasis raktas
    public static BigInteger genE(BigInteger phi) {
        Random rand = new Random();
        BigInteger e = new BigInteger(1024, rand);
        do {
            e = new BigInteger(1024, rand);
            while (e.min(phi).equals(phi)) { // jei phi mažesnis nei e, ieško naujo e
                e = new BigInteger(1024, rand);
            }
        } while (!gcd(e, phi).equals(BigInteger.ONE));
        return e;
    }

    //Užkodavimas
    public static BigInteger encrypt(BigInteger message, BigInteger e, BigInteger n) {
        return message.modPow(e, n);
    }

    //Dekodavimas
    public static BigInteger decrypt(BigInteger message, BigInteger d, BigInteger n) {
        return message.modPow(d, n);
    }

    //Apskaičiuojame n iš p ir q
    public static BigInteger n(BigInteger p, BigInteger q) {
        return p.multiply(q);

    }


    // Viešojo rakto įrašymas į failą
    private void toFile(String fileName, BigInteger exp) throws IOException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            fos = new FileOutputStream(fileName);
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
            oos.writeObject(exp);
            System.out.println(fileName + " sugeneruota sėkmingai!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                oos.close();
                if (fos != null) {
                    fos.close();
                }
            }
        }

    }

    //Resultato/ušifruoto teksto įrašymas į failą
    private void result (String fileName, BigInteger result) throws IOException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            fos = new FileOutputStream(fileName);
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
            oos.writeObject(result);
            System.out.println(fileName + " sugeneruota sėkmingai!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                oos.close();
                if (fos != null) {
                    fos.close();
                }
            }
        }

    }
}