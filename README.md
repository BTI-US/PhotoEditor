# PhotoEditor

![Github Action](https://github.com/BTI-US/PhotoEditor/actions/workflows/app_build_and_test.yml/badge.svg)
[![Downloads](https://img.shields.io/badge/Download-3.0.2-blue.svg)](https://search.maven.org/artifact/com.burhanrashid52/photoeditor/3.0.2/aar) ![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg) [![JavaDoc](https://img.shields.io/badge/JavaDoc-PhotoEditor-blue.svg)](https://burhanrashid52.github.io/PhotoEditor/) [![Uplabs](https://img.shields.io/badge/Uplabs-PhotoEditor-orange.svg)](https://www.uplabs.com/posts/photoeditor)
[![AndroidArsenal](https://img.shields.io/badge/Android%20Arsenal-PhotoEditor-blue.svg)](https://android-arsenal.com/details/1/6736)
[![AndroidDevDigest](https://img.shields.io/badge/AndroidDev%20Digest-%23185-brightgreen.svg)](https://www.androiddevdigest.com/digest-185)
[![AwesomeAndroid](https://img.shields.io/badge/Awesome%20Android-%2397-red.svg)](https://android.libhunt.com/newsletter/97)
[![AndroidWeekly](https://img.shields.io/badge/Android%20Weekly-%23312-blue.svg)](http://androidweekly.net/issues/issue-312)
[![Mindorks](https://img.shields.io/badge/Mindorks%20Newsletter-%234-ff69b4.svg)](https://mindorks.com/newsletter/edition/4)

A Photo Editor library with simple, easy support for image editing using Paints, Text, Filters, Emoji and Sticker-like stories.

[Download link](https://f005.backblazeb2.com/file/BTI-Platform/PhotoEditor-v.1.1.4.apk)

![](https://i.imgur.com/ZYtLHTZ.png)

## Features

- [**Drawing**](#drawing) on the image with options to change its Brush's Color, Size, Opacity, Erasing and basic shapes.
- Apply [**Filter Effect**](#filter-effect) on the image using MediaEffect
- Adding/Editing [**Text**](#text) with options to change its Color with Custom Fonts.
- Adding [**Emoji**](#emoji) with Custom Emoji Fonts.
- Adding [**Images/Stickers**](#adding-imagesstickers)
- Pinch to Scale and Rotate views.
- [**Undo and Redo**](#undo-and-redo) for Brush and Views.
- [**Deleting**](#deleting) Views
- [**Saving**](#saving) Photo after editing.
- More [**FAQ**](#faq).

## Benefits

- Hassle free coding
- Increase efficiency
- Easy image editing

## Getting Started

To start with this, we need to simply add the dependencies from `mavenCentral()` in the gradle file of our app module like this:
```groovy
implementation 'com.burhanrashid52:photoeditor:3.0.2'
```
We can also import the `:photoeditor` module from the sample for further customization

## Migrations

### AndroidX

PhotoEditor [v.1.0.0](https://github.com/burhanrashid52/PhotoEditor/releases/tag/v.1.0.0) is a migration to androidX and dropping the support of the older support library. There are no API changes. If you find any issues migrating to v.1.0.0, please follow this [Guide](https://developer.android.com/jetpack/androidx/migrate). If you still facing the issue then you can always rollback to [v.0.4.0](https://github.com/burhanrashid52/PhotoEditor/releases/tag/v.0.4.0). Any fix in PR is welcomed :)

### Kotlin

PhotoEditor [v.2.0.0](https://github.com/burhanrashid52/PhotoEditor/releases/tag/v.2.0.0) is fully migrated to Kotlin. You can use [v.1.5.1](https://github.com/burhanrashid52/PhotoEditor/releases/tag/v.1.5.1) for the Java version. There are no breaking API changes in these two versions.

## Setting up the View

First, we need to add `PhotoEditorView` in our xml layout

```xml
 <ja.burhanrashid52.photoeditor.PhotoEditorView
        android:id="@+id/photoEditorView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:photo_src="@drawable/got_s" />
  
```
We can define our drawable or color resource directly using `app:photo_src`

We can set the image programmatically by getting sources from `PhotoEditorView` which will return an `ImageView` so that we can load images from resources, files or (Picasso/Glide)
```kotlin
PhotoEditorView mPhotoEditorView = findViewById(R.id.photoEditorView);

mPhotoEditorView.getSource().setImageResource(R.drawable.got);
```

## Building a PhotoEditor

To use the image editing feature we need to build a PhotoEditor which requires a Context and PhotoEditorView which we have to set up in our xml layout

```kotlin
//Use custom font using latest support library
Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);

//loading font from asset
Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");

mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
         .setPinchTextScalable(true)
         .setClipSourceImage(true)
         .setDefaultTextTypeface(mTextRobotoTf)
         .setDefaultEmojiTypeface(mEmojiTypeFace)
         .build();
 ```
We can customize the properties in the PhotoEditor as per our requirement

| Property  | Usage |
| ------------- | ------------- |
| `setPinchTextScalable()`  | set false to disable pinch to zoom on text insertion. Default: true. |
| `setClipSourceImage()` | set true to clip the drawing brush to the source image. Default: false. |
| `setDefaultTextTypeface()`  | set default text font to be added on image  |
| `setDefaultEmojiTypeface()`  | set default font specifc to add emojis |

That's it we are done with setting up our library

## Drawing

We can customize our brush and paint with different sets of properties. To start drawing on the image we need to enable the drawing mode

![](https://i.imgur.com/INi5LIy.gif)

| Type                                         | Method                                                                 |
|----------------------------------------------|------------------------------------------------------------------------|
| Enable/Disable                               | `mPhotoEditor.setBrushDrawingMode(true);`                              |
| Shape (brush, line, oval, rectangle, arrow)  | `mPhotoEditor.addShape(shape)`                                         |
| Shape size (px)                              | `mPhotoEditor.setBrushSize(brushSize)` or through the a ShapeBuilder   |
| Shape opacity (In %)                         | `mPhotoEditor.setOpacity(opacity)` or through the a ShapeBuilder       |
| Shape color                                  | `mPhotoEditor.setBrushColor(colorCode)` or through the a ShapeBuilder  |
| Brush Eraser                                 | `mPhotoEditor.brushEraser()`                                           |

**Note**: Whenever we set any property of a brush for drawing it will automatically enable the drawing mode

## Shapes

We can draw shapes from [v.1.5.0](https://github.com/burhanrashid52/PhotoEditor/releases/tag/v.1.5.0). We use `ShapeBuilder` to define shapes and other properties.

![](https://im2.ezgif.com/tmp/ezgif-2-5d5f7ddbe72e.gif)

```kotlin
val shapeBuilder = ShapeBuilder()
    .withShapeOpacity(100)
    .withShapeType(ShapeType.Oval)
    .withShapeSize(50f);

photoEditor.setShape(mShapeBuilder)
```
For more details check [ShapeBuilder](https://github.com/BTI-US/PhotoEditor/blob/master/photoeditor/src/main/java/ja/burhanrashid52/photoeditor/shape/ShapeBuilder.kt).

## Filter Effect

We can apply an in-build filter to the source images using 

 `mPhotoEditor.setFilterEffect(PhotoFilter.BRIGHTNESS);`

![](https://i.imgur.com/xXTGcVC.gif)

We can also apply custom effects using `Custom.Builder`

For more details check [Custom Filters](https://github.com/BTI-US/PhotoEditor/wiki/Filter-Effect)

## Text

![](https://i.imgur.com/491BmE8.gif)

We can add the text with inputText and colorCode like this

`mPhotoEditor.addText(inputText, colorCode);` 

It will take the default fonts provided in the builder. If we want different fonts for different text we can set the typeface with each text like this

`mPhotoEditor.addText(mTypeface,inputText, colorCode);`

In order to edit the text we need the view, which we will receive in our PhotoEditor callback. This callback will trigger when we **Long Press** the added text

 ```kotlin
 mPhotoEditor.setOnPhotoEditorListener(new OnPhotoEditorListener() {
            @Override
            public void onEditTextChangeListener(View rootView, String text, int colorCode) {
                
            }
        });
  ```
Now we can edit the text with a view like this

`mPhotoEditor.editText(rootView, inputText, colorCode);`

If you want more customization on text. Please refer to the wiki page for more details.

## Emoji

![](https://i.imgur.com/RP8kqz6.gif)

We can add the Emoji by `PhotoEditor.getEmojis(getActivity());` which will return a list of emojis Unicode.

`mPhotoEditor.addEmoji(emojiUnicode);`

It will take the default fonts provided in the builder. If we want different Emoji fonts for different emoji we can set the typeface with each Emoji like this

`mPhotoEditor.addEmoji(mEmojiTypeface,emojiUnicode);`

## Adding Images/Stickers

We need to provide a Bitmap to add our Images  `mPhotoEditor.addImage(bitmap);`

## Undo and Redo

![](https://i.imgur.com/1Y9WcCB.gif)

 ```kotlin
   mPhotoEditor.undo();
   mPhotoEditor.redo();
 ```

## Deleting
For deleting a Text/Emoji/Image we can click on the view to toggle the view highlighter box which will have a close icon. So, by clicking on the icon we can delete the view.

## Saving

In [v.3.0.0](https://github.com/burhanrashid52/PhotoEditor/releases/tag/v.3.0.0) onward, we can save an image to a file using coroutines:

```kotlin
// Please note that if you call this from a fragment, you should call
// 'viewLifecycleOwner.lifecycleScope.launch' instead.
lifecycleScope.launch {
    val result = photoEditor.saveAsFile(filePath)
    if (result is SaveFileResult.Success) {
        showSnackbar("Image saved!")
    } else {
        showSnackbar("Couldn't save image")
    }
}
```

You can also save an image to a file from Java. We need to provide a file with a callback method when the edited image is saved.

   ```java
    mPhotoEditor.saveAsFile(filePath, new PhotoEditor.OnSaveListener() {
                    @Override
                    public void onSuccess(@NonNull String imagePath) {
                       Log.e("PhotoEditor","Image Saved Successfully");
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("PhotoEditor","Failed to save Image");
                    }
                });
```

For more details see [Saving](https://github.com/BTI-US/PhotoEditor/wiki/Saving)

## How to contribute?

* Check out contribution guidelines 👉[CONTRIBUTING.md](https://github.com/BTI-US/PhotoEditor/blob/master/CONTRIBUTING.md)

## FAQ

<details><summary>Can I use this library in my app for free?</summary>
<p>

Yes. It's an open-source library and free to use. If this library has saved your time then showing a little credit will increase my motivation towards making the library better :)

</p>
</details>

<details><summary>Does it support the CROP feature?</summary>
<p>

Currently, No. I started to build in branch [PE-79](https://github.com/burhanrashid52/PhotoEditor/issues/79), however due to time constraints, I dropped the idea. Any PR related to CROP is welcomed :)

</p>
</details>

<details><summary>Facing issues in applying Filter?</summary>
<p>

The filter effect is applied using `GlSurfaceView` and the implementation of this feature causes a lot of issues. Need to think of some other alternative solution. Here is the issue [list](https://github.com/burhanrashid52/PhotoEditor/issues?q=is%3Aissue+is%3Aopen+filter).

</p>
</details>

<details><summary>Does it support for other platforms (iOS, Web, Flutter)?</summary>
<p>

No. Currently, the focus is on making the android library better. We don't have any plans for [other Platform](https://github.com/burhanrashid52/PhotoEditor/issues/24).

</p>
</details>

<details><summary>Other Know Issues</summary>
<p>

[Image Scaling](https://github.com/burhanrashid52/PhotoEditor/issues/10).
<br>[Memory Issue in Filter](https://github.com/burhanrashid52/PhotoEditor/issues/48).

</p>
</details>

## MIT License

Copyright (c) 2024 BTI Platform

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 
