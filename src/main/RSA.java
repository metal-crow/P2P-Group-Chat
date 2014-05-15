package main;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;


public class RSA {
	
	private BigInteger e,n,d;
	private String name;
	
	//constructor to generate public and private keys (this user's RSA instance)
	public RSA(int bitlength){
		//generate two primes
		BigInteger prime1=generateprime(bitlength);
		BigInteger prime2=generateprime(bitlength);
		
		//Compute n = prime1*prime2 
		n = prime1.multiply(prime2);
		
		//Compute the totient of prime1 and prime2
		BigInteger totient = (prime1.subtract(BigInteger.ONE)).multiply(prime2.subtract(BigInteger.ONE));
		
		//get 1< e <totient that is coprime to the totient by generating a prime, then checking it isn't a product of totient.
		//can just start at 3 and go up, as small numbers aren't more secure than larger numbers here
		e= new BigInteger("3");
		while(totient.mod(e).equals(BigInteger.ZERO)){
			e=e.add(new BigInteger("2"));
		}
		
		//compute the modular multiplicative inverse. 1/e % totient
		//TODO this will sometimes return "java.lang.ArithmeticException: BigInteger not invertible."
		d = e.modInverse(totient);
	}
	
	//if the user is given a public key
	public RSA(BigInteger n, BigInteger e, String name){
		this.n=n;
		this.e=e;
		this.name=name;
	}
	
	private BigInteger generateprime(int keylength){
		int RSAkeyLength=keylength/8;
		boolean foundprime=true;
		
		byte[] Prime_Num_bytearray = new byte[RSAkeyLength];
		new SecureRandom().nextBytes(Prime_Num_bytearray);

		//know that a prime must be odd and positive
		Prime_Num_bytearray[0] &= ~(1 << 7); //convert to unsigned byte (make positive)
		Prime_Num_bytearray[RSAkeyLength-1] |= (1 << 0); //make right most bit 1 (make odd)
		
		BigInteger Prime_Num = new BigInteger(Prime_Num_bytearray);
		
		do{
			foundprime=true;
			
			//increment up by 2 to next odd number
			Prime_Num=Prime_Num.add(new BigInteger("2"));
			
			//Fermat primality test
			int test=0;
			int certanty=5;
			while(foundprime && test<certanty){
				
				BigInteger a = new BigInteger((RSAkeyLength*8)-1,new Random()).add(BigInteger.ONE);//get a, where 1<=a<Prime_Num
				
				//if we have a Fermat witness for the compositeness of n
				if(!a.modPow(Prime_Num.subtract(BigInteger.ONE), Prime_Num).equals(BigInteger.ONE)){
					foundprime=false;
				}
				
				test++;
			}
			
			//perform Miller-Rabin tests
			//while(foundprime)
			
			//probably prime at this point, deterministic check
			
		
		} while(!foundprime);
		
		return Prime_Num;
	}
	
	public BigInteger Encrypt(String code){
		if(code.length()>0){
			BigInteger code_numeric = new BigInteger(code.getBytes());

			BigInteger encrypted_numeric = code_numeric.modPow(e, n);
		
			return encrypted_numeric;
		}
		else{
			return BigInteger.ZERO;
		}
	}
	
	public String Decrypt(BigInteger code){
		return new String(code.modPow(d, n).toByteArray());
	}
	
	public BigInteger[] publickey(){
		return new BigInteger[] {n,e};
	}
	
	public String name(){
		return name;
	}

}
