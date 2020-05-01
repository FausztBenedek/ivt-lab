package hu.bme.mit.spaceship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Any;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class GT4500Test {

  private GT4500 ship;
  private TorpedoStore secondaryTorpedoStore;
  private TorpedoStore primaryTorpedoStore;
  /**
   * Helper variables
   */
  private Integer fireCounter;

  @BeforeEach
  public void init(){
     primaryTorpedoStore = mock(TorpedoStore.class);
     secondaryTorpedoStore = mock(TorpedoStore.class);

    this.ship = new GT4500(primaryTorpedoStore, secondaryTorpedoStore);
  }

  @Test
  public void fireTorpedo_Single_Success(){
    // Arrange
    when(primaryTorpedoStore.isEmpty()).thenReturn(false);
    when(primaryTorpedoStore.fire(1)).thenReturn(true);

    // Act
    boolean result = ship.fireTorpedo(FiringMode.SINGLE);

    // Assert
    assertEquals(true, result);

    verify(primaryTorpedoStore, times(1)).fire(1);
    verify(secondaryTorpedoStore, never()).fire(anyInt());
  }

  @Test
  public void fireTorpedo_All_Success(){
    // Arrange
    when(primaryTorpedoStore.isEmpty()).thenReturn(false);
    when(secondaryTorpedoStore.isEmpty()).thenReturn(false);
    when(primaryTorpedoStore.fire(1)).thenReturn(true);
    when(secondaryTorpedoStore.fire(1)).thenReturn(true);

    // Act
    boolean result = ship.fireTorpedo(FiringMode.ALL);

    // Assert
    assertEquals(true, result);
    verify(primaryTorpedoStore, times(1)).fire(1);
    verify(secondaryTorpedoStore, times(1)).fire(1);
  }

  /**
   * 1.	Amikor az ágyút SINGLE módban először sütjük el és van elég töltény, akkor az elsődleges ágyú tüzel
   */
  @Test
  public void testCase1() {
      when(primaryTorpedoStore.fire(1)).thenReturn(true);
      when(primaryTorpedoStore.isEmpty()).thenReturn(false);

      boolean success = ship.fireTorpedo(FiringMode.SINGLE);

      assertTrue(success);
      verify(primaryTorpedoStore, times(1)).fire(1);
      verify(secondaryTorpedoStore, never()).fire(anyInt());
  }

  /**
   * 2.	Amikor az ágyút SINGLE módban kétszer sütjük el és van elég töltény, akkor a másodlagos és az elsődleges ágyú is tüzelt egyszer
   */
  @Test
  public void testCase2() {
    when(primaryTorpedoStore.fire(1)).thenReturn(true);
    when(primaryTorpedoStore.isEmpty()).thenReturn(false);
    when(secondaryTorpedoStore.fire(1)).thenReturn(true);
    when(secondaryTorpedoStore.isEmpty()).thenReturn(false);

    boolean first = ship.fireTorpedo(FiringMode.SINGLE);
    boolean second = ship.fireTorpedo(FiringMode.SINGLE);

    assertTrue(first && second);
    verify(primaryTorpedoStore, times(1)).fire(1);
    verify(secondaryTorpedoStore, times(1)).fire(1);
  }

  /**
   * 3.	Amikor az ágyút SINGLE módban háromszor sütjük el, de az elsődleges ágyúban csak egy töltény volt a kiinduló
   * állapotban, akkor a másodlagos kétszer, az elsődleges ágyú egyszer tüzel.
   */
  @Test
  public void testCase3() {
    fireCounter = 1;
    when(primaryTorpedoStore.fire(1)).then(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
        boolean empty = fireCounter > 0;
        fireCounter--;
        return empty;
      }
    });
    when(primaryTorpedoStore.isEmpty()).then(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
        return fireCounter <= 0;
      }
    });
    when(secondaryTorpedoStore.fire(1)).thenReturn(true);
    when(secondaryTorpedoStore.isEmpty()).thenReturn(false);

    boolean first = ship.fireTorpedo(FiringMode.SINGLE);
    boolean second = ship.fireTorpedo(FiringMode.SINGLE);
    boolean third = ship.fireTorpedo(FiringMode.SINGLE);

    assertTrue(first && second && third);
    verify(primaryTorpedoStore, times(1)).fire(1);
    verify(secondaryTorpedoStore, times(2)).fire(1);
  }

  /**
   * 4.	Amikor az ágyút SINGLE módban először sütjük el és van elég töltény, viszont csődöt mond, akkor a másodlagos ágyú nem tüzel
   */
  @Test
  public void tescCase4() {
    when(primaryTorpedoStore.fire(1)).thenReturn(false); // Unsuccessful shot
    when(primaryTorpedoStore.isEmpty()).thenReturn(false);

    ship.fireTorpedo(FiringMode.SINGLE);

    verify(secondaryTorpedoStore, never()).fire(anyInt());
  }

  /**
   * 5.	Amikor az ágyút ALL módban sütjük el, de az elsődlegesben nincs elég töltény, akkor is sikeresnek számít a tüzelés, és a másodlagos ágyú tüzel.
   */
  @Test
  public void testCase5() {
    when(primaryTorpedoStore.fire(1)).thenReturn(true);
    when(primaryTorpedoStore.isEmpty()).thenReturn(true);
    when(secondaryTorpedoStore.fire(1)).thenReturn(true);
    when(secondaryTorpedoStore.isEmpty()).thenReturn(false);

    boolean success = ship.fireTorpedo(FiringMode.ALL);

    assertTrue(success);
    verify(secondaryTorpedoStore, times(1)).fire(1);
    verify(primaryTorpedoStore, never()).fire(anyInt());

  }
}
