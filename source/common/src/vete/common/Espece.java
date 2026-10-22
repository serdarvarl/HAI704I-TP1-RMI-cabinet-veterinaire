package vete.common;

import java.io.Serializable;

public class Espece implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nameAnimal;
    private int esperanceAnimvalVie;

    public Espece(String nameAnimal, int esperanceAnimvalVie){

        this.nameAnimal =  nameAnimal;
        this.esperanceAnimvalVie = esperanceAnimvalVie;        
    }

    public String getNom() {
        return nameAnimal;
    }

    public int getEsperanceVie() {
        return esperanceAnimvalVie;
    }

    public void setEsperanceVie(int esperanceVie) {
        this.esperanceAnimvalVie = esperanceVie;
    }

    @Override
    public String toString() {
        return "Espece(" + nameAnimal + ", " + esperanceAnimvalVie + " ans)";
    }



}
