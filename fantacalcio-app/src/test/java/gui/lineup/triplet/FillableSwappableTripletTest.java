package gui.lineup.triplet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import domainModel.Player.Defender;
import gui.lineup.chooser.LineUpChooser.SubstituteSelectorDelegate;
import gui.lineup.sequence.FillableSwappableSequence;
import gui.lineup.sequence.FillableSwappableSequence.FillableSwappableSequenceListener;

@DisplayName("A FillableSwappableTriplet")
@ExtendWith(MockitoExtension.class)
public class FillableSwappableTripletTest {
	
	// horizontal dependencies
	private @Mock SubstituteSelectorDelegate<Defender> mockFillable1, mockFillable2, mockFillable3;
	private @Mock FillableSwappableSequence<SubstituteSelectorDelegate<Defender>> mockSequence;
	
	// vertical dependency
	private @Mock FillableSwappableTripletWidget mockWidget;

	// the SUT reference
	private FillableSwappableTriplet<Defender> triplet;

	@BeforeEach
	public void testCaseSpecificSetup() {
		triplet = new FillableSwappableTriplet<Defender>(
				mockFillable1, mockFillable2, mockFillable3);
		triplet.setWidget(mockWidget);
	}
	
	@Nested
	@DisplayName("as a SubstituteTripletChooserDelegate")
	class AsDelegate {
	
		@Test
		@DisplayName("initializes its internal Sequence")
		void WhenInitialized() {
	
			try (@SuppressWarnings("rawtypes")
			MockedStatic<FillableSwappableSequence> mockedStaticSequence = mockStatic(FillableSwappableSequence.class)) {
	
				// GIVEN FillableSwappableSequence's factory method returns the mocked Sequence
				when(FillableSwappableSequence.createSequence(List.of(mockFillable1, mockFillable2, mockFillable3)))
						.thenReturn(mockSequence);
	
				// WHEN the SUT is told to initialize on a new Sequence
				triplet.initSequence();
	
				// THEN a call to FillableSwappableSequence.createSequence is made with the
				// right arguments
				mockedStaticSequence.verify(() -> FillableSwappableSequence
						.createSequence(List.of(mockFillable1, mockFillable2, mockFillable3)));
	
				// AND the Sequence returned by that call is attached a Listener
				verify(mockSequence).attachListener(any());
	
				// AND the Widget is told to reset swapping
				verify(mockWidget).resetSwapping();
			}
		}
		
		@Test
		@DisplayName("returns the \"next-fillable\" Selector")
		void askedForNextFillable(@Mock SubstituteSelectorDelegate<Defender> mockSelector) {
			
			// GIVEN the SUT has NOT been initialized yet
			
			// WHEN asked to return the next-fillable
			ThrowingCallable shouldThrow = () -> triplet.getNextFillable();
			
			// THEN it throws
			assertThatThrownBy(shouldThrow).isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("not yet been asked to initialize its internal FillableSwappableSequence");
			
			// BUT GIVEN the SUT has been initialized
			try (@SuppressWarnings("rawtypes")
			MockedStatic<FillableSwappableSequence> mockedStaticSequence = mockStatic(
					FillableSwappableSequence.class)) {
				when(FillableSwappableSequence.createSequence(List.of(mockFillable1, mockFillable2, mockFillable3)))
						.thenReturn(mockSequence);
				triplet.initSequence();
			}
			
			// AND the mocked Sequence returns a certain next-fillable
			when(mockSequence.nextFillable()).thenReturn(Optional.of(mockSelector));			
			
			// WHEN asked to return the next-fillable
			Optional<SubstituteSelectorDelegate<Defender>> nextFillableSelector = triplet.getNextFillable();
			
			// THEN it correctly relays FillableSwappableSequence.nextFillable()
			assertThat(nextFillableSelector).contains(mockSelector);		
		}
	}
	
	/**
	 * TEST ISOLATION
	 * these tests make the following assumptions about the SUT:
	 * 
	 * 	1. when calling initSequence()
	 * 		i.  it calls into {@link FillableSwappableSequence#createSequence(List)} with 
	 * 			a List of its three composed {@link SubstituteSelectorDelegate}s
	 * 		ii. it sets a {@link FillableSwappableSequenceListener} into the
	 * 			Sequence returned by that call
	 */
	@Nested
	@DisplayName("once its internal sequence is initialized")
	class OnceInitialized {		
		
		private @Captor ArgumentCaptor<FillableSwappableSequenceListener<SubstituteSelectorDelegate<Defender>>> listenerCaptor;
		
		@BeforeEach
		void testCaseSpecificSetup() {			
			
			try (@SuppressWarnings("rawtypes")
			MockedStatic<FillableSwappableSequence> mockedStaticSequence = mockStatic(
					FillableSwappableSequence.class)) {

				// stubs static factory method
				when(FillableSwappableSequence.createSequence(List.of(mockFillable1, mockFillable2, mockFillable3)))
						.thenReturn(mockSequence);

				// causes SUT to invoke static factory method
				triplet.initSequence();
			}
			
			// captures Sequence Listener
			verify(mockSequence).attachListener(listenerCaptor.capture());
		}
	
		@Nested
		@DisplayName("reacts to notifications from the Sequence")
		class FromSequenceToWidget {	
			
			@Nested
			@DisplayName("enabling swpping")
			class Enabled {
				
				@Nested
				@DisplayName("when notfied that")
				class OnNotifiaction {
					
					@Test
					@DisplayName("the second fillable was filled")
					public void selector2Filled() {
						
						// WHEN driver notifies second selector filled
						listenerCaptor.getValue().becameFilled(mockFillable2);
						
						// THEN the Widget is requested to enable swapping first pair
						verify(mockWidget).setSwappingFirstPair(true);
					}
					
					@Test
					@DisplayName("the third fillable was filled")
					public void selector3Filled() {
						
						// WHEN driver notifies third selector filled
						listenerCaptor.getValue().becameFilled(mockFillable3);
						
						// THEN the Widget is requested to enable swapping first pair
						verify(mockWidget).setSwappingSecondPair(true);
					}
				}
			}
			
			@Nested
			@DisplayName("disabling swapping")
			class Disabled {
				
				@Nested
				@DisplayName("when notfied that")
				class OnNotifiaction {
					
					@Test
					@DisplayName("the third fillable was emptied")
					public void selector3Emptied() {
						
						// WHEN driver notifies third selector emptied
						listenerCaptor.getValue().becameEmpty(mockFillable3);
						
						// THEN the Widget is requested to disable swapping first pair
						verify(mockWidget).setSwappingSecondPair(false);
					}
					
					@Test
					@DisplayName("the second fillable was emptied")
					public void selector2Emptied() {
						
						// WHEN driver notifies second selector emptied
						listenerCaptor.getValue().becameEmpty(mockFillable2);
						
						// THEN the Widget is requested to disable swapping first pair
						verify(mockWidget).setSwappingFirstPair(false);
					}
				}
			}
		}
	
		@Nested
		@DisplayName("forwards Widget swap requests to Sequence")
		class FromWidgetToSequence {
			
			@Nested
			@DisplayName("when asked by user to")
			class OnUserClickTo {
	
				@Test
				@DisplayName("swap the first selector pair")
				public void swapSelectors1And2() {
	
					// WHEN Widget requests swapping first pair
					triplet.swapFirstPair();
	
					// THEN the correct swap request is sent to the sequence
					verify(mockSequence).swapRight(mockFillable1);
				}
	
				@Test
				@DisplayName("swap the second selector pair")
				public void swapSelectors2And3() {
	
					// WHEN user clicks second swap button
					triplet.swapSecondPair();
	
					// THEN the correct swap request is sent to the sequence
					verify(mockSequence).swapRight(mockFillable2);
				}
			}
		}
	}
}
