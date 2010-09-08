/*
 * * * *  HashTable Member Functions  * * * *
 */

function Hashtable(){
    this._listBuckets = new Array();
    this._listCapacities = new Array(2, 3, 5, 7, 17, 37, 67, 131, 257, 521, 1031, 2053, 4099, 8209);
    this._intCapacityIndex = 4;
    this._intCapacity = this._listCapacities[this._intCapacityIndex];
	this._intFilled = 0;
	this._intSize = 0;
	this._intAdded = 0;
	this._intRemoved = 0;

	/**
	 * Add the specified key/value pair to the table
	 */
	this.put = function(key, value){
        var elementNew = new this.HashElement(key, value);
        var intIndex = this.hash(key); 
        elementNew._intIndex = intIndex;

        var listBucket = this._listBuckets[intIndex];
		if( undefined == listBucket ){
			listBucket = new Array();
			this._listBuckets[intIndex] = listBucket;
			this._intFilled++;
        }

        var boolAdded = false;
		for(var i=0; i < listBucket.length; i++){
			if( listBucket[i]._key == elementNew._key ){
				listBucket[i] = elementNew;
				boolAdded = true;
            }
        }
		if( !boolAdded ){
            listBucket.push(elementNew);        
            this._intSize++;
        }
		this._intAdded++;
    };
    
    /**
     * Wrapper for put(), which parses the input XML key/value pair and inserts it
     */
    this.putXML = function(strElementXML){
        var listComponents;
    	if( listComponents = strElementXML.match( /<element><key>(.*)<\/key><value>(.*)<\/value><\/element>/ ) ){
		    this.put(listComponents[1], listComponents[2]);
        }
    };

    /**
     * Returns the value stored for the specified key
     */
	this.get = function(key){
        var intIndex = this.hash(key);
        var listBucket = this._listBuckets[intIndex];
		if( undefined == listBucket ){
			return undefined;
        }
        for(var i=0; i < listBucket.length; i++){
            if( listBucket[i]._key == key ){
				return listBucket[i]._value;
            }
        }
        return undefined;
    };

    /**
     * Removes the key/value pair for the specified key from the table
     */
	this.remove = function(key){
        var intIndex = this.hash(key);
		if( undefined == this._listBuckets[intIndex] ){
			return undefined;
        }
        var listBucket = this._listBuckets[intIndex];
		for(var i=0; i < listBucket.length; i++){
			if( key == listBucket[i]._key ){
                var element = listBucket[i];
                listBucket.splice(i, 1);
                this._intRemoved++;
				return element._value;
            }
        }
		return undefined;
    }

	/*
	 * * * *  Hash Functions  * * * *
	 */
	this.hash3 = function(key){
        var intIndex = 0;
        for(var i=0; i < key.length; i++){
		    intIndex += key.charCodeAt(i) * Math.pow(10, i);
        }
	    intIndex %= this._intCapacity;
        return intIndex;
    };

	this.hash2 = function(key){
        var intIndex = 0;
        for(var i=0; i < key.length; i++){
		    intIndex += key.charCodeAt(i);
        }
	    intIndex %= this._intCapacity;
        return intIndex;
    };

	this.hash1 = function(key){
        var intIndex = 0;
        for(var i=0; i < key.length; i++){
		    intIndex += key.charCodeAt(i) * (0 == i? 1 : 2 * i);
        }
	    intIndex %= this._intCapacity;
        return intIndex;
    };

    this.hash = this.hash2;

    /**
     * Returns an Array containing all keys in the table
     */
    this.listKeys = function() {
    	var listKeys = new Array();
    	for(i in this._listBuckets){
    		var listBucket = this._listBuckets[i];
    		if( undefined == listBucket ){ continue; }
    		for(j in listBucket){ listKeys.push( listBucket[j]._key ); }
    	}
    	return listKeys;
    }
    
    /*
     * * * *  Accessors  * * * *
     */
	this.getUtilization = function() { return Math.round(10000.0 * (this._intFilled / this._intCapacity)) / 100.0; }
	this.getStorage = function()     { return Math.round(10000.0 * (this._intSize / this._intCapacity)) / 100.0; }
	this.getSize = function()        { return this._intSize; }
	this.getCapacity = function()    { return this._intCapacity; }

	/**
	 * Change the number of table buckets, in the specified direction, and repopulate the table 
	 */	
    this.resize = function(boolIncrease){
        if( (boolIncrease && this._listCapacities.length - 1 <= this._intCapacityIndex) ||
            (!boolIncrease && 0 == this._intCapacityIndex) ){
		    return false;
        }

        var listBucketsOld = this._listBuckets;
		this._listBuckets = new Array();
        this._intCapacityIndex += (boolIncrease? 1 : -1);
		this._intCapacity = this._listCapacities[this._intCapacityIndex];
        this._intFilled = 0;
        this._intSize = 0;
        this._intAdded = 0;
        this._intRemoved = 0;
		for(var i=0; i < listBucketsOld.length; i++){
			if( undefined == listBucketsOld[i] ){
				continue;
            }
            var listBucket = listBucketsOld[i];
			for(var j=0; j < listBucket.length; j++){
				this.put(listBucket[j]._key, listBucket[j]._value);
            }
        }
		return true;
    };

    /**
     * Data structure for storing a key/value pair and meta-data
     */
	this.HashElement = function(key, value){
        this._key = key;
        this._value = value;
        this._intIndex = -1;
		this._strXML = "<element><key>" + this._key + "</key><value>" + this._value + "</value></element>";
    };
}

