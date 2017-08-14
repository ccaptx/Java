#include "Main.h"
#include <string.h>
#include <graphviz/gvc.h>

JNIEXPORT void JNICALL Java_Main_toSVG
  (JNIEnv * env, jobject obj, jstring string, jstring fileName)
{
    const char *str = (*env)->GetStringUTFChars(env,string,0);
    const char *file = (*env)->GetStringUTFChars(env,fileName,0);

    printf("graph:-->%s\n",str);
    printf("fileName: -->\n",file);

    GVC_t *gvc;
    graph_t *g;
    FILE *fp;

    gvc = gvContext();

    g = agmemread(str);

    gvLayout(gvc, g, "dot");
    
    fp = fopen(file, "w");

    gvRender(gvc, g, "svg", fp);

    gvFreeLayout(gvc, g);

    agclose(g);

    gvFreeContext(gvc);
    
    (*env)->ReleaseStringUTFChars(env,string,str);
 }

void main() {}
