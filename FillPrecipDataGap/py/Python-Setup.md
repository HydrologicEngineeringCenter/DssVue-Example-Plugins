
https://github.com/winpython/winpython/releases/download/2.3.20200530/Winpython64-3.7.7.1dot.exe
-- start commmand prompt 

C:\Programs\WPy64-3771\WinPython Command Prompt.exe

pip install https://github.com/gyanz/pydsstools/zipball/master 
pip install plotly
pip install psutil
pip install requests
-- oraca is for some graphics if needed for your project ---
download orca from: https://github.com/plotly/orca/releases  
   grab get windows-release.zip
   unzip two layers deep (not using installer)
   unzip app-64.7z
   put orace.exe in path (example C:\Programs\orca\app-64)




:: run simple test program.

python a.py

====== a.py ====================
from pydsstools.heclib.dss.HecDss import Open

dss_file = "example.dss"

pathname_pattern ="/*/*/*/*/*/*/"

with Open(dss_file) as fid:
    path_list = fid.getPathnameList(pathname_pattern,sort=1)
    print('list = %r' % path_list)