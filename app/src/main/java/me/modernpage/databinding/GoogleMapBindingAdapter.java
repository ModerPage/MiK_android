package me.modernpage.databinding;

import androidx.databinding.BindingAdapter;

import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;

import me.modernpage.ui.googlemap.GoogleMapActivity;

public class GoogleMapBindingAdapter {

    @BindingAdapter(value = {"onEditorAction"})
    public static void setOnEditorActionListener(PlacesAutocompleteTextView view, GoogleMapActivity.GoogleMapHandler handler) {
        if (handler != null) {
            view.setOnEditorActionListener((textView, i, keyEvent) -> {
                handler.onEditorAction(textView, i, keyEvent);
                return false;
            });
        }
    }

    @BindingAdapter("onPlaceSelected")
    public static void setOnPlaceSelectedListener(PlacesAutocompleteTextView view, DetailsCallback callback) {
        if (callback != null) {
            view.setOnPlaceSelectedListener(place -> view.getDetailsFor(place, callback));
        }
    }
}
