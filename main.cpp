#include "mbed.h"
#include "stdlib.h"

//DigitalOut myled(LED1);
DigitalIn RX(p13);
DigitalIn TX(p14);

Serial pc(USBTX, USBRX);
Serial bt(p13, p14);

char    messageBuffer[10];
int     nbChar = 0;
char    charBuffer;
int     smallestDist = 1000000;

struct Moteur {
    int distance;
    int sens;
    bool loaded;
    int pas;
};

int dist;
int sens;

Moteur  mA = Moteur(); // ou m1
Moteur  mB = Moteur(); // ou m2
Moteur  mC = Moteur(); // ou m3

// MOTEUR

// intialisation des pins
/* U1 : Droite */
DigitalOut in_1(p7); 
DigitalOut in_2(p8);  
DigitalOut in_3(p9);
DigitalOut in_4(p10);
DigitalOut myled(LED1);

/* U3 : Milieu */
DigitalOut in_9(p27); 
DigitalOut in_10(p28);  
DigitalOut in_11(p29);
DigitalOut in_12(p30);

/* U2 : Gauche */
DigitalOut in_5(p21); 
DigitalOut in_6(p22);  
DigitalOut in_7(p23);
DigitalOut in_8(p24);

void reglage_pins_m1(int pin1, int pin2, int pin3, int pin4)
{
    in_1 = pin1; //output : 1 -> 2A Bleu | (Jaune) 
    in_2 = pin2; //output : 2 -> 2B Jaune| (Bleu)
    in_3 = pin3; //output : 3 -> 1A Rouge| (Rose)
    in_4 = pin4; //output : 4 -> 1B Vert | (Orange)
}

void reglage_pins_m2(int pin1, int pin2, int pin3, int pin4)
{
    in_5 = pin1; //output : 1 -> 2A Bleu | (Jaune) 
    in_6 = pin2; //output : 2 -> 2B Jaune| (Bleu)
    in_7 = pin3; //output : 3 -> 1A Rouge| (Rose)
    in_8 = pin4; //output : 4 -> 1B Vert | (Orange)
}

void reglage_pins_m3(int pin1, int pin2, int pin3, int pin4)
{
    in_9 = pin1; //output : 1 -> 2A Bleu | (Jaune) 
    in_10 = pin2; //output : 2 -> 2B Jaune| (Bleu)
    in_11 = pin3; //output : 3 -> 1A Rouge| (Rose)
    in_12 = pin4; //output : 4 -> 1B Vert | (Orange)
}

void prochainStep_m1(int numero) //Mode entrainement complet
{
    if (numero == 0) {
        reglage_pins_m1(0, 1, 1, 0); // J, B, R, O

    }
    if (numero == 1) {
        reglage_pins_m1(1, 0, 1, 0);

      
    }
    if (numero == 2) {
        reglage_pins_m1(1, 0, 0, 1);
        

    }
    if (numero == 3) {
        reglage_pins_m1(0, 1, 0, 1);

    }
}


void prochainStep_m2(int numero) //Mode entrainement complet
{
    if (numero == 0) {
        reglage_pins_m2(0, 1, 1, 0); // J, B, R, O

    }
    if (numero == 1) {
        reglage_pins_m2(1, 0, 1, 0);

      
    }
    if (numero == 2) {
        reglage_pins_m2(1, 0, 0, 1);
        

    }
    if (numero == 3) {
        reglage_pins_m2(0, 1, 0, 1);

    }
}


void prochainStep_m3(int numero) //Mode entrainement complet
{
    if (numero == 0) {
        reglage_pins_m3(0, 1, 1, 0); // J, B, R, O

    }
    if (numero == 1) {
        reglage_pins_m3(1, 0, 1, 0);

      
    }
    if (numero == 2) {
        reglage_pins_m3(1, 0, 0, 1);
        

    }
    if (numero == 3) {
        reglage_pins_m3(0, 1, 0, 1);

    }
}

void marche_avant_m1(float attente, int nombre_de_pas) //Fait monter
{
    for (int i=0; i <= nombre_de_pas; i++) {
        prochainStep_m1(i % 4);
        wait(attente);
    }
}

void marche_avant_m2(float attente, int nombre_de_pas) //Fait monter
{
    for (int i=0; i <= nombre_de_pas; i++) {
        prochainStep_m2(i % 4);
        wait(attente);
    }
}

void marche_avant_m3(float attente, int nombre_de_pas) //Fait monter
{
    for (int i=0; i <= nombre_de_pas; i++) {
        prochainStep_m3(i % 4);
        wait(attente);
    }
}

void marche_arriere_m1(float attente, int nombre_de_pas) //Fait descendre
{
    for (int i=0; i <= nombre_de_pas; i++) {
        prochainStep_m1(3 - (i % 4));
        wait(attente);
    }
}


void marche_arriere_m2(float attente, int nombre_de_pas) //Fait descendre
{
    for (int i=0; i <= nombre_de_pas; i++) {
        prochainStep_m2(3 - (i % 4));
        wait(attente);
    }
}


void marche_arriere_m3(float attente, int nombre_de_pas) //Fait descendre
{
    for (int i=0; i <= nombre_de_pas; i++) {
        prochainStep_m3(3 - (i % 4));
        wait(attente);
    }
}

void setDataMoteur(char);
void moveBot();
// UN MESSAGE EST DE CETTE FORME : Nom du moteur + Sens deplacement + Valeur déplacement + '?' pour désigner la fin du message

int main() {
    pc.baud(115200);
    bt.baud(115200);
    pc.printf("Hello World!\n\r");
    bt.printf("Hello World!\r\n");
    mA.loaded = false;
    mB.loaded = false;
    mC.loaded = false;
    
    // TO-DO : AJOUTER UNE FONCTION POUR CALIBRAGE AU DEMARRAGE
    
    while(1)
    {   
        // Pour chaque caractere de la string venant de l'applu
        charBuffer = bt.getc();
        pc.printf("%c\n",charBuffer);
        if (charBuffer == '?') { // Fin de message
            // Récupération du moteur
            char moteur = messageBuffer[0];
            // Récupération sens de deplacement
            sens = messageBuffer[1];
            // Récupération de la valeur de déplacement
            int z = 0;
            char buff[nbChar - 2];
            for (int j = 2; j < nbChar; j++) {
                buff[z++] = messageBuffer[j];
            }
            dist = atoi(buff);
            if (dist < smallestDist)
                smallestDist = dist;
            setDataMoteur(moteur);
            // Si on a les donnees des 3 moteurs
            if (mA.loaded && mB.loaded && mC.loaded) {
                // Bouger le robot
                moveBot();
                // Reset datas
                setDataMoteur('x');
                smallestDist = 1000000;
            }
            // Reset pour next message
            nbChar = 0;
        } else {
            messageBuffer[nbChar++] = charBuffer;
        }
        
    }
    return 0;
}

// Permet de reset les objets Moteur pour le mrochain déplacement
void setDataMoteur(char c) {
    switch(c) {
        case 'A':
            if(sens == '0') {
                mA.sens = false;
            }else {
                mA.sens = true;
            }
            mA.distance = dist;
            mA.loaded = true;
            mA.pas = dist/smallestDist;
            break;
        case 'B':
            if(sens == '0') {
                mB.sens = false;
            }else {
                mB.sens = true;
            }
            mB.distance = dist;
            mB.loaded = true;
            mB.pas = dist/smallestDist;
            break;
        case 'C':
            if(sens == '0') {
                mC.sens = false;
            }else {
                mC.sens = true;
            }
            mC.distance = dist;
            mC.loaded = true;
            mC.pas = dist/smallestDist;
            break;
        default:
            mA.loaded = false;
            mB.loaded = false;
            mC.loaded = false;
    }
}

// Une fois que les objets Moteurs on été construit, on utilise leurs données pour Bouger le robot
void moveBot() {
    bool done = false;
    while(!done) {
        done = true;
        if (mA.distance > 0) {
            done = false;
            if (mA.sens) {
                marche_arriere_m1(0.01, 1);
            } else {
                marche_avant_m1(0.01, 1);
            }
            mA.distance -= mA.pas;
        }
        if (mB.distance > 0) {
            done = false;
            if (mB.sens) {
                marche_arriere_m2(0.05, 1);
            } else {
                marche_avant_m2(0.05, 1);
            }
            mB.distance -= mB.pas;
        }
        if (mC.distance > 0) {
            done = false;
            if (mC.sens) {
                marche_arriere_m3(0.05, 1);
            } else {
                marche_avant_m3(0.05, 1);
            }
            mC.distance -= mC.pas;
        }
    }
}