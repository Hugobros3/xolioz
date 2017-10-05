package io.xol.dogez.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UnpackDefaults {

	public UnpackDefaults(File folder) {
		try {
			
			InputStream is = this.getClass().getResourceAsStream("/default/defaultFiles.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			String l;
			while((l = reader.readLine()) != null) {
				InputStream fis = this.getClass().getResourceAsStream("/default/" + l);
				FileOutputStream fos = new FileOutputStream(new File(folder.getAbsolutePath() + "/" + l));
				
				byte[] buffer = new byte[1024];
			    int r;
			    while ((r = fis.read(buffer)) != -1) {
			        fos.write(buffer, 0, r);
			    }
			    
			    fos.close();
			}
			
			System.out.println("Unpacked " + l);
			
		}
		catch(IOException e) {
			
		}
	}

}
