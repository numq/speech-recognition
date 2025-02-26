package selector

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import interaction.SpeechRecognitionItem

@Composable
fun SpeechRecognitionItemSelector(
    modifier: Modifier,
    selectedSpeechRecognitionItem: SpeechRecognitionItem,
    selectSpeechRecognitionItem: (SpeechRecognitionItem) -> Unit,
) {
    Selector(
        modifier = modifier,
        items = SpeechRecognitionItem.entries.map(SpeechRecognitionItem::name),
        selectedIndex = SpeechRecognitionItem.entries.indexOf(selectedSpeechRecognitionItem),
        selectIndex = { index -> selectSpeechRecognitionItem(SpeechRecognitionItem.entries.elementAt(index)) }
    )
}