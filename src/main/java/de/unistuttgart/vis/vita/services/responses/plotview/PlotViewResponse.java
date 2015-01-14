package de.unistuttgart.vis.vita.services.responses.plotview;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PlotViewResponse {
  @XmlElement(name="characters")
  private List<PlotViewCharacter> characters;

  @XmlElement
  private int panels;

  @XmlElement
  private List<PlotViewScene> scenes;

  public PlotViewResponse() {
    characters = new ArrayList<>();
    scenes = new ArrayList<>();
  }

  public PlotViewResponse(List<PlotViewCharacter> characters, int panels,
      List<PlotViewScene> scenes) {
    this.characters = characters;
    this.panels = panels;
    this.scenes = scenes;
  }

  public List<PlotViewCharacter> getCharacters() {
    return characters;
  }

  public void setCharacters(List<PlotViewCharacter> characters) {
    this.characters = characters;
  }

  public int getPanels() {
    return panels;
  }

  public void setPanels(int panels) {
    this.panels = panels;
  }

  public List<PlotViewScene> getScenes() {
    return scenes;
  }

  public void setScenes(List<PlotViewScene> scenes) {
    this.scenes = scenes;
  }
}
