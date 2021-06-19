package de.darmstadt.tk

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.darmstadt.tk.data.Event
import de.darmstadt.tk.ui.theme.SensingAppTheme

@Composable
fun MainScreen(startTracking: () -> Unit, eventList: MutableList<Event>) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Button(onClick = startTracking) {
            Text(text = "Request permissions")
        }


        if (eventList.isEmpty())
            Text(
                text = "No events Captured yet!",
                style = MaterialTheme.typography.body1
            )
        else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(eventList.asReversed()) { e ->
                    EventCard(e)
                }
            }
        }
    }
}


@Composable
fun EventCard(
    event: Event,
) {
    Card(elevation = 3.dp, modifier = Modifier.padding(vertical = 4.dp)) {
        Column(
            Modifier
                .animateContentSize() // automatically animate size when it changes

//            .border(1.dp, Color.Black, RectangleShape)
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    event.name,
                    style = MaterialTheme.typography.caption,
                )
            }

            Column(Modifier.padding(horizontal = 6.dp)) {
                Text(
                    text = event.desc,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SensingAppTheme {
        Greeting("Android")
    }
}