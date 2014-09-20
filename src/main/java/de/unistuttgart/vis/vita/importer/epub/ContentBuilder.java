package de.unistuttgart.vis.vita.importer.epub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ContentBuilder {
   
  public String getStringFromInputStream(InputStream is) {
    BufferedReader bufferedReader = null;
    StringBuilder stringBuilder = new StringBuilder();

    String line;
    try {

      bufferedReader = new BufferedReader(new InputStreamReader(is));
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return stringBuilder.toString();

  }
}
