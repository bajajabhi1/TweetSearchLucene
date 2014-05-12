'''
Created on May 11, 2014

@author: Abhinav
'''

from os import listdir
from os.path import isfile, join

def main():
    fileList = list()
    mapList = list()
    p30List = list()
    p20List = list()
    p10List = list()
    onlyfiles = [ f for f in listdir('eval') if isfile(join('eval',f)) ]
    for fileName in onlyfiles:
        fileDict = dict()
        p30Dict = dict()
        p20Dict = dict()
        p10Dict = dict()
        mapDict = dict()
        fileNameTmp = fileName[:fileName.find('.gz.txt')]
        if fileName.find('queryExpansionBingAPI_Top') > -1:
            fileNameTmp = fileNameTmp[fileName.find('queryExpansionBingAPI_Top')+22:]
        print fileNameTmp
        fileDict['file'] = fileNameTmp
        p30Dict['file'] = fileNameTmp
        p20Dict['file'] = fileNameTmp
        p10Dict['file'] = fileNameTmp
        mapDict['file'] = fileNameTmp
        print fileName
        f = open(join('evalFiles', fileName))
        for line in f:
            #print line
            if line.find('all map') > -1:
                idx = line.find('all map')
                print 'map:'+line[idx+8:].strip()
                fileDict['map'] = line[idx+8:].strip()
                mapDict['value'] = line[idx+8:].strip()
            if line.find('all P_10') > -1:
                idx = line.find('all P_10')
                print 'P_10:'+line[idx+9:].strip()
                fileDict['P_10'] = line[idx+9:].strip()
                p10Dict['value'] = line[idx+9:].strip()
            if line.find('all P_20') > -1:
                idx = line.find('all P_20')
                print 'P_20:'+line[idx+9:].strip()
                fileDict['P_20'] = line[idx+9:].strip()
                p20Dict['value'] = line[idx+9:].strip()
            if line.find('all P_30') > -1:
                idx = line.find('all P_30')
                print 'P_30:'+line[idx+9:].strip()
                fileDict['P_30'] = line[idx+9:].strip()
                p30Dict['value'] = line[idx+9:].strip()
        fileList.append(fileDict)
        p30List.append(p30Dict)
        p20List.append(p20Dict)
        p10List.append(p10Dict)
        mapList.append(mapDict)
    
    print fileList
    print mapList
    print p30List
    print p20List
    print p10List
    createCsv(mapList,p30List,p20List,p10List)

def createCsv(mapList,p30List,p20List,p10List):
    outputFile = open('output.csv','w')
    print >> outputFile, 'MAP Scores'
    for mapDict in mapList:
        print >> outputFile, mapDict['file']+ ',' + mapDict['value']
    
    print >> outputFile, ','
    print >> outputFile, 'P30 Scores'
    for p30Dict in p30List:
        print >> outputFile, p30Dict['file']+ ',' + p30Dict['value']
    
    print >> outputFile, ','
    print >> outputFile, 'P20 Scores'
    for p20Dict in p20List:
        print >> outputFile, p20Dict['file']+ ',' + p20Dict['value']
    
    print >> outputFile, ','
    print >> outputFile, 'P10 Scores' 
    for p10Dict in p10List:
        print >> outputFile, p10Dict['file']+ ',' + p10Dict['value']
    
    outputFile.close()
    
if __name__ == "__main__":
    main()
