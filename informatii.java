package com.oracle.jcclassic.samples.wallet;

public class informatii {
	public short codMaterie;
	public short notaMaterie;
	public short zi;
	public short luna;
	public short an;
	public short codConcurs;
	public short punctajConcurs;
	//short counterNote=0;
	
	public informatii(short codMaterie) {
		this.codMaterie = codMaterie;
	}
	
	public void addNota(short nota, short zi, short luna, short an) {
		notaMaterie = nota;
		this.zi = zi;
		this.luna = luna;
		this.an = an;
	}
	public void addPunctaj(short punctaj) {
		punctajConcurs = punctaj;
	}
	public void addNota(short nota) {
		notaMaterie = nota;
	}
}
