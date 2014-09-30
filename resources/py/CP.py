'''
Created on 15 janv. 2014

@author: malsarem
'''


import rdflib
from sktensor import dtensor, sptensor
from rdflib.term import URIRef
from sktensor import cp_als
import array
import gc
import sys
import logging
import os
import numpy as np
import numpy
import sys, traceback

from numpy.ma.core import count
from numpy.core.numeric import ones


class RDF:
    def rdfPreProccessing(self,g):
        total=sum(1 for x in g.triples((None, None, None)))
        logging.warning("Size before preProccessing: "+total.__str__()+" Triple")
        precision=0.01 # 40%
        logging.warning( "precision: "+ (precision*100).__str__()+"%")
        for p in g.predicates(None, None):
            count=sum(1 for x in g.triples((None, p, None)))
            if count>=precision*total and p.find("ensen.org") == -1 :
                g.remove((None, p, None))
                logging.warning( count.__str__()+ "- removing: "+p)
        after=sum(1 for x in g.triples((None, None, None)))
        logging.warning( "Size after precision preProccessing: "+after.__str__()+" Triple")
        logging.warning( "Reduction Rate: "+((total-after)*100/(total*1.0)).__str__()+"%")
        
        #cut preproccessing
        toBeRemoved =[]
        objs=g.objects(None,None)
        for o in objs:
            count1=sum(1 for x in g.triples((None, None, o)))# in
            count2=sum(1 for x in g.triples((o, None, None)))# out
            #print o,count
            if(count1<2 and count2==0):
                toBeRemoved.append(o)
        #print toBeRemoved
        for o in toBeRemoved:
            g.remove((None, None, o))
        
        after=sum(1 for x in g.triples((None, None, None)))
        logging.warning( "Size after cut preProccessing: "+after.__str__()+" Triple")
        logging.warning( "Reduction Rate: "+((total-after)*100/(total*1.0)).__str__()+"%")
        #path=os.path.dirname(os.path.realpath(__file__))
        #g.serialize(path+"\test.rdf", format="xml")      
       
        
        return g
    def RDFParsing(self,path,isSparse):
        #parse RDF       
        g=rdflib.Graph()
        g.parse(file=open(path, "r"),
                format="application/rdf+xml")        
        entities = []       
        predicates = []
       
        for s in g.subjects(None, None):
            ss=s.encode('utf-8').__str__()
            if  ss not in entities:                
                entities.extend([ss])                
        for o in g.objects(None, None):
            oo=o.encode('utf-8').__str__()
            if type(o).__name__ != "Literal" :
                if oo not in entities:
                    entities.extend([oo])
        for p in g.predicates(None, None):           
            pp=p.encode('utf-8').__str__()
            if pp not in predicates:                
                predicates.extend([pp])        
       
        entities = np.array(entities)
        
        #print entities    
        logging.warning( "*************************"  )  
        predicates = np.array(predicates)        
        #print predicates    
        
        #prepare tensor frontal slices as np matrices
        lenentities = len(entities)
        lenpredicates = len(predicates)               
        logging.warning("Tensor: "+str(lenentities)+" X "+str(lenentities)+" X "+str(lenpredicates))        
        T = np.zeros((lenentities, lenentities, lenpredicates),dtype=np.int)           
        print "entities".join(map(str, entities))
        
        for s,p,o in g:
            try:
                logging.warning("try to build the tensor")
                ss=s.__str__().encode('utf-8')
                oo=o.__str__().encode('utf-8')
                pp=p.__str__().encode('utf-8')
                i,j,k=entities.tolist().index(ss),entities.tolist().index(oo),predicates.tolist().index(pp)
                #logging.warning("ijk".join(map(str, [i,j,k])))                
                #print "ijk".join(map(str, [i,j,k]))
                value=1.0
                if "__" in p:
                    #print p.split("__")[len(p.split("__"))-1]
                    value=float(p.split("__")[len(p.split("__"))-1])
                    #print "value"+value
                T[i, j, k] = value
            except:
                #printException()        
                continue
        if isSparse:
            xyz, c = self.sparsed(T)
            logging.warning("xyz: ")
            logging.warning("-".join(map(str, xyz)))
            #logging.warning("c: ".join(c))
            Tensor = sptensor(xyz, c, shape=(lenentities, lenentities, lenpredicates), dtype=np.int)                
        else:
            Tensor = dtensor(T)        
        #print "*************************"
        #print Tensor[:,:,slice] 
        return Tensor,g,entities,predicates
    def sparsed(self, T):
        indices = T.nonzero()
        x = list(indices[0])
        y = list(indices[1])
        z = list(indices[2])
        xyz = (x, y, z)
        c = np.ones(len(x), dtype=np.int)
        return xyz, c
    def printAxe(self,name,axe):
        print name
        for value in axe:
            print value
    
class cp:
    def claculateCP(self,T,rank):
        logging.warning("claculateCP")
        P, fit, itr, exectimes = cp_als (T, rank, init='random', fit_method='full')
        logging.warning("*****************Tensor*****************************")
        logging.warning(T.shape)
        logging.warning("******************** U1 represent the subjects *************************")
        logging.warning( P.U[0].shape)
        #print(P.U[0])
        logging.warning("********************U2 represent the objects **************************")
        logging.warning(P.U[1].shape)
        #print(P.U[1])
        logging.warning("********************U3 represent the predicats **************************")
        logging.warning( P.U[2].shape)
        #print(P.U[2])
        logging.warning("********************L represent the subjects **************************")
        #print(P.lmbda)
        logging.warning("Done with "+itr.__str__()+" iterations")
        return P
    def buildDecompositedTensors(self,P,rank,entities,predicates): 
        Ts= []
        t0=np.array(P.U[0])
        t1=np.array(P.U[1])
        t2=np.array(P.U[2])
        Tk=np.zeros((entities, entities, predicates))
        for k in range(rank):
            for i in range(entities):
                for j in range(entities):
                    for l in range(predicates):
                        Tk[i,j,l]=P.lmbda[k]*t0[i,k]*t1[j,k]*t2[l,k]
            Ts.append(Tk)
            logging.warning( "Ts: "+k.__str__()   )         
        return Ts
    def recomposeTensor(self,Ts,rank,entites,predicats):
        newTensor=np.zeros((entites, entites, predicats))
        for ti in range(rank):
            newTensor=np.add(newTensor,Ts[ti])
        logging.warning( newTensor )        
    def findMaxScore(self,s,o,p,Ts): #s,o,p are ids, Ts are array of generated tensors
        max=0
        for ti in Ts:
            value = ti[s,o,p] 
            if value>max:
                max = value
        return max
    def rankTriples(self,Ts,g,entities,predicates):
        rankedTriples=dict()
        for s,p,o in g:           
            score= self.findMaxScore(numpy.where(entities==s)[0], numpy.where(entities==o)[0], numpy.where(predicates==p)[0], Ts)
            if score > 0: 
                rankedTriples.update({(s.__str__()+" --> "+p.__str__()+" --> "+ o.__str__()):score})
                #print s+" --> "+p+" --> "+ o
                #print score
        rankedTriples= sorted(rankedTriples.iteritems(), key=lambda (k,v): (v,k))
        for i in range(len(rankedTriples)-1, 0, -1):            
            logging.warning(rankedTriples[i][1].__str__() +" : "+rankedTriples[i][0].__str__())
    
    #def rankTriples1(self,Ts,g,entities,predicates): # maximize the predicate score
    
    def buildAuthHubGroups(self,P,rank,g,entities,predicates,limit): # maximize the predicate score    
        
        for r in range(rank):
            print( "Group "+r.__str__())              
            print( "    Predicates ")
            rankedPredicates=dict()
            U2=np.array(P.U[2])[:,r]
            for i in range(len(U2)):
                if U2[i]>limit:
                    rankedPredicates.update({predicates[i]:U2[i]})           
            rankedPredicates= sorted(rankedPredicates.iteritems(), key=lambda (k,v): (v,k), reverse=True)           
            self.printDic(rankedPredicates,"")
            
            print( "    Authority ")
            rankedObjects=dict()         
            U1=np.array(P.U[1])[:,r]
            for i in range(len(U1)):
                if U1[i]>limit:
                    rankedObjects.update({entities[i]:U1[i]}) 
            rankedObjects= sorted(rankedObjects.iteritems(), key=lambda (k,v): (v,k), reverse=True)
            self.printDic(rankedObjects,"")
            
            print( "    Hubs ")
            rankedObjects=dict()         
            U0=np.array(P.U[0])[:,r]
            for i in range(len(U0)):
                if U0[i]>limit:
                    rankedObjects.update({entities[i]:U0[i]}) 
            rankedObjects= sorted(rankedObjects.iteritems(), key=lambda (k,v): (v,k), reverse=True)
            self.printDic(rankedObjects,"")
            
    def cp_withoutPreDefRank(self,T):        
        rank=1
        for r in range(1,50):        
            logging.warning( "Testing the rank "+r.__str__())
            P= self.claculateCP(T,r) 
            logging.warning( "T norm "+dtensor(T.toarray()).norm().__str__())
            logging.warning( "P norm "+P.norm().__str__())
            gc.collect()
            #if np.allclose(dtensor(T.toarray()), dtensor(P.toarray()),atol=1e-3,rtol=1e-5):
            if (P.norm()*100/dtensor(T.toarray()).norm())>95:
                logging.warning( "Rank founded : "+r.__str__())
                rank = r
                break
            logging.warning( "not a good rank")
            P=None
        return P,rank
    def generateTensor(self):
        entities = ["Alex","Bob","chris","Don","Elly"]
        predicates = ["Loves","Hates"]
        T = np.zeros((5, 5, 2))
        T[:, :, 0] = [[0,0,0,1,1], [0,0,0,0,0], [0,0,0,0,0], [1,0,0,0,0], [1,0,0,0,0]]
        T[:, :, 1] = [[0,1,0,0,0], [0,0,1,0,0], [0,1,0,0,0], [0,0,1,0,0], [0,1,1,0,0]]
        T = dtensor(T)
        return T,None,entities,predicates
    def printDic(self,dic,pre):
        for keys,values in dic:
            print ( pre+""+keys.__str__()  +" : "+ values.__str__() ) 
    def printAll(self,P):
        print("U0")
        logging.warning("U0")
        print(P.U[0].shape[0])
        print(P.U[0].shape[1])
        print(self.myString(P.U[0]))       
       
        print("U1")    
        logging.warning("U1, ")    
        print( P.U[1].shape[0])
        print( P.U[1].shape[1])
        print(self.myString(P.U[1]))
        #print(P.U[1])         
        print("U2") 
        logging.warning("U2, ")       
        print( P.U[2].shape[0])
        print( P.U[2].shape[1])
        print(self.myString(P.U[2]))
        #print(P.U[2])         
        print("U3")#lmbda 
        logging.warning("U3, ")      
        print( P.lmbda.shape[0] )
        print(' '.join([str(c) for c in P.lmbda]))  
        logging.warning("end")         
    def myString(self,myArray):
        return '\n'.join([' '.join([str(c) for c in lst]) for lst in myArray.tolist()])

def printException():
    exc_type, exc_value, exc_traceback = sys.exc_info()
    print "*** print_tb:"
    traceback.print_tb(exc_traceback, limit=1, file=sys.stdout)
    print "*** print_exception:"
    traceback.print_exception(exc_type, exc_value, exc_traceback,
                              limit=2, file=sys.stdout)
    print "*** print_exc:"
    traceback.print_exc()
    print "*** format_exc, first and last line:"
    formatted_lines = traceback.format_exc().splitlines()
    print formatted_lines[0]
    print formatted_lines[-1]
    print "*** format_exception:"
    print repr(traceback.format_exception(exc_type, exc_value,
                                          exc_traceback))
    print "*** extract_tb:"
    print repr(traceback.extract_tb(exc_traceback))
    print "*** format_tb:"
    print repr(traceback.format_tb(exc_traceback))
    print "*** tb_lineno:", exc_traceback.tb_lineno    
path=os.path.dirname(os.path.realpath(__file__))
logging.basicConfig(filename=path+'/log/alllog.log',level=logging.DEBUG)
logging.warning("Start Python\n")   

rdf= RDF()
cp= cp()

if(len(sys.argv)==1):
    RDFfile=path+'/RDFfile.rdf'
else:
    RDFfile=sys.argv[1]
logging.warning("read the file "+RDFfile+"\n")
T,g,entities,predicates=  rdf.RDFParsing(RDFfile,True)
logging.warning("Done RDFParsing: ")
if entities ==None or predicates==None :
    print None 
else:   
    rdf.printAxe("entities",entities) 
    rdf.printAxe("predicates",predicates)
    logging.warning(str(entities.__len__()))
    logging.warning("entities, ")
    logging.warning(str(predicates.__len__()))
    logging.warning("predicates, \n")
    rank=10
    #P,rank=cp.cp_withoutPreDefRank(T)
    P= cp.claculateCP(T,rank)
    cp.printAll(P)
    cp.buildAuthHubGroups(P,rank,g,entities,predicates,0.1)

