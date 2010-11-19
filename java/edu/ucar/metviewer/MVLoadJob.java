package edu.ucar.metviewer;

import java.sql.Connection;

public class MVLoadJob{
	
	protected Connection _con = null;

	protected String _strMetVersion				= "V2.0";

	protected String _strDBHost					= "";
	protected String _strDBName					= "";
	protected String _strDBUser					= "";
	protected String _strDBPassword				= "";
	
	protected boolean _boolLineTypeLoad			= false;
	protected MVOrderedMap _mapLineTypeLoad		= new MVOrderedMap();
	
	protected boolean _boolVerbose				= false;
	protected int _intInsertSize				= 1;
	protected boolean _boolStatHeaderTableCheck	= true;
	protected boolean _boolStatHeaderDBCheck	= false;
	protected boolean _boolModeHeaderDBCheck	= true;
	protected boolean _boolDropIndexes			= false;
	protected boolean _boolApplyIndexes			= true;
	
	protected String _strFolderTmpl				= "";
	protected MVOrderedMap _mapLoadVal			= new MVOrderedMap();
	
	protected String[] _listLoadFiles			= {};
	
	public Connection getConnection()									{ return _con;											}
	public void		setConnection(Connection con)						{ _con = con;											}

	public String	getMetVersion()										{ return _strMetVersion;								}															
	public void		setMetVersion(String metVersion)					{ _strMetVersion = metVersion;							}	
	
	public String	getDBHost()											{ return _strDBHost;									}															
	public void		setDBHost(String dbHost)							{ _strDBHost = dbHost;									}
	public String	getDBName()											{ return _strDBName;									}															
	public void		setDBName(String dbName)							{ _strDBName = dbName;									}
	public String	getDBUser()											{ return _strDBUser;									}															
	public void		setDBUser(String dbUser)							{ _strDBUser = dbUser;									}
	public String	getDBPassword()										{ return _strDBPassword;								}															
	public void		setDBPassword(String dbPassword)					{ _strDBPassword = dbPassword;							}
	
	public boolean	getLineTypeLoad()									{ return _boolLineTypeLoad;								}
	public void		setLineTypeLoad(boolean lineTypeLoad)				{ _boolLineTypeLoad = lineTypeLoad;						}
	public MVOrderedMap getLineTypeLoadMap()							{ return _mapLineTypeLoad;								}
	public void addLineTypeLoad(String type)							{ _mapLineTypeLoad.put(type, new Boolean(true));		}
	public void removeLineTypeLoad(String type)							{ _mapLineTypeLoad.remove(type);						}
	public void clearLineTypeLoad()										{ _mapLineTypeLoad = new MVOrderedMap();				}

	public boolean	getVerbose()										{ return _boolVerbose;									}
	public void		setVerbose(boolean verbose)							{ _boolVerbose = verbose;								}
	public int		getInsertSize()										{ return _intInsertSize;								}
	public void		setInsertSize(int insertSize)						{ _intInsertSize = insertSize;							}
	public boolean	getStatHeaderTableCheck()							{ return _boolStatHeaderTableCheck;						}
	public void		setStatHeaderTableCheck(boolean statHeaderTableCheck){ _boolStatHeaderTableCheck = statHeaderTableCheck;	}
	public boolean	getStatHeaderDBCheck()								{ return _boolStatHeaderDBCheck;						}
	public void		setStatHeaderDBCheck(boolean statHeaderDBCheck)		{ _boolStatHeaderDBCheck = statHeaderDBCheck;			}
	public boolean	getModeHeaderDBCheck()								{ return _boolModeHeaderDBCheck;						}
	public void		setModeHeaderDBCheck(boolean modeHeaderDBCheck)		{ _boolModeHeaderDBCheck = modeHeaderDBCheck;			}
	public boolean	getDropIndexes()									{ return _boolDropIndexes;								}
	public void		setDropIndexes(boolean dropIndexes)					{ _boolDropIndexes = dropIndexes;						}
	public boolean	getApplyIndexes()									{ return _boolApplyIndexes;								}
	public void		setApplyIndexes(boolean applyIndexes)				{ _boolApplyIndexes = applyIndexes;						}

	public String	getFolderTmpl()										{ return _strFolderTmpl;								}															
	public void		setFolderTmpl(String folderTmpl)					{ _strFolderTmpl = folderTmpl;							}

	public MVOrderedMap getLoadVal()									{ return _mapLoadVal;									}
	public void addLoadVal(String field, String[] vals, int index)		{ _mapLoadVal.put(field, vals, index);					}
	public void addLoadVal(String field, String[] vals)					{ addLoadVal(field, vals, _mapLoadVal.size());			}
	public void removeLoadVal(String field)								{ _mapLoadVal.remove(field);							}
	public void clearLoadVal()											{ _mapLoadVal = new MVOrderedMap();						}
	
	public String[]	getLoadFiles()										{ return _listLoadFiles;								}
	public void		setLoadFiles(String[] loadFiles)					{ _listLoadFiles = loadFiles;							}
}