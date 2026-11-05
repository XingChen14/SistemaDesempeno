Actividad: 
Implementar un sistema que estime el desempeño de cualquier modelo de clasificación.

Requerimientos: 

1. El sistema debe incluir una interfaz de usuario (IU) para solicitar y desplegar información, incluyendo cualquier tipo de error generado. El usuario no debe interactuar de ninguna forma, con la consola 5-10pts.
-Para consultar algunos criterios de diseño de IU ver: https://www.nngroup.com/articles/ten-usability-heuristics/
Si el sistema no incluye interfaz de usuario, y solo se interactúa con consola, el valor de este requerimiento es: 0 pts

2. El sistema solicita al usuario, el nombre del archivo de entrada (en formato csv), y la cantidad de clases del problema de clasificación. Si consideras que debe solicitar otra información, deberás justificarlo durante la presentación de tu demo. (0-2 pts.)  

Si no lo solicita, y lo dejan fijo o tienen que modificar el programa, este requerimiento tiene valor de 0 pts.

3. Este archivo de entrada  csv contiene los resultados procesados por un modelo de ML preentrenado y corresponden a un dataset de prueba. Por lo tanto este archivo tiene el siguiente formato (similar a Tabla 1 de archivo anexo el cual realizaron en el ejercicio de este lunes): 

ID-Dato, Clase verdadera, Clase Predicción 

El sistema debe ser capaz de leer archivos csv de 3 columnas, y de cualquier cantidad de registros

0-3 pts. Inclumplimiento total  de este requerimiento es 0 pts.

4. El sistema debe evaluar el resultado de predicción de cada dato de prueba y almacenar los resultados en archivos csv, un archivo por cada clase. El formato de estos archivos csv debe ser similar al de las Tablas 2, 3 y 4 del archivo anexo (tal como se realizó en el ejercicio de este lunes).

Nota: 6) El sistema debe preguntar al usuario como quiere nombrar a cada uno de los archivos. 

 (0-15 pts)

5. El sistema deberá tener dos modos de ejecución:

a) Toma el resultado del 1er dato de prueba, lo evalúa con respecto a cada clase, y muestra el resultado en pantalla, esperando a que el usuario de una indicación (p.ej. presionar un botón diseñado para este propósito) para continuar con el siguiente dato de prueba. Y así sucesivamente, el usuario, podrá visualizar en pantalla, el resultado de evaluar cada dato de prueba. (0-10 pts.)

Adicionalmente, deberá tener otro botón, para indicar, procese lo restante, y otro que sirva para terminar este proceso. (0-5 pts)

b) Se solicita que procese todo el dataset. (0-10 pts.)

6) Como resultado final, por evaluar todos los resultados del dataset, el sistema debe estimar el desempeño de clasificación con las siguientes métricas, y almacenarlas en 1 archivo ResultadosFinal csv, que el usuario puede elegir  renombrarlo desde la interfaz de usuario:  

a) Suma de TP, Suma de FP, Suma de FP, Suma de FN, por cada una de las Clases  (0-10 pts)
b) Precision, Recall, Accuracy, F-Score, por cada una de las Clases (0-10 pts)
c) Promedio general del modelo en Precision, Recall, Accuracy y F-Score (0-10 pts)

Este archivo csv deberá contener los resultados de a), b, y c), respectivamente. (0-5 pts; si no es posible  indicar el nombre del archivo desde la Interfaz de Usuario, se otorgará máximo 2 pts.)

7) Generar la matriz de Confusión que represente los aciertos y confusiones entre Clases y almacenarlo en archivo csv. Es decir, si el problema tiene N clases, la matriz debe ser de N x N.

0-10 pts., y mostrarla en pantalla en algún formato adecuado para que pueda ser incluida directamente a los reportes que realizarás mas adelante.  

===================================

ASEGURATE DE: Realizar varias pruebas a tu sistema con el ejemplo dado en clase. Genera tus propias tablas de entrada para que puedas incrementar las pruebas de tu sistema. Antes de presentar tu demo, asegúrate que tu sistema funciona para cualquier archivo de entrada. 

NOTA: NO PUEDES UTILIZAR LIBRERIAS QUE FACILITEN ESTIMAR CUALQUIERA DE LAS MÉTRICAS. DE SER ASI, ESTA PRÁCTICA SE CALIFICA CON 0.

===================================

Entregable 1: 
El ejercicio anexo en documento se sube resuelto. Se entrega el jueves próximo - 29 de octubre


Entregable 2: 
Presentar el demo del sistema durante el horario de sus laboratorio (presencial, pasan por orden de lista). Jueves 5 de noviembre.