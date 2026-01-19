#!/bin/bash
# quick-dmg.sh - Quick DMG creation

echo "Quick DMG Creation for 3D Model Editor"

cd /Users/nevermore/Developer/Education/Java/Simple3DViewer

# Create dist folder if doesn't exist
mkdir -p dist

# Build if needed
if [ ! -f "target/3d-model-editor.jar" ]; then
    echo "Building project..."
    mvn clean package -DskipTests
fi

# Create simple DMG structure
rm -rf dmg_temp
mkdir -p dmg_temp

# Check if JAR exists
if [ ! -f "target/3d-model-editor.jar" ]; then
    echo "❌ JAR file not found!"
    echo "Trying to find alternative JAR..."

    # Look for any JAR file
    JAR_FILE=$(find target -name "*.jar" -type f | head -n 1)
    if [ -z "$JAR_FILE" ]; then
        echo "❌ No JAR files found in target/"
        exit 1
    fi
    echo "✅ Found JAR: $JAR_FILE"
    cp "$JAR_FILE" dmg_temp/3d-model-editor.jar
else
    cp target/3d-model-editor.jar dmg_temp/
fi

# Create simple .app
echo "Creating .app bundle..."
mkdir -p "dmg_temp/3D Model Editor.app/Contents/MacOS"
mkdir -p "dmg_temp/3D Model Editor.app/Contents/Resources"

# Create Info.plist
cat > "dmg_temp/3D Model Editor.app/Contents/Info.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>3D Model Editor</string>
    <key>CFBundleIdentifier</key>
    <string>com.cgvsu.3dmodeleditor</string>
    <key>CFBundleVersion</key>
    <string>1.0.0</string>
    <key>CFBundleExecutable</key>
    <string>3DModelEditor</string>
</dict>
</plist>
EOF

# Create launcher script
cat > "dmg_temp/3D Model Editor.app/Contents/MacOS/3DModelEditor" << 'EOF'
#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/../Resources"
java -jar 3d-model-editor.jar
EOF

chmod +x "dmg_temp/3D Model Editor.app/Contents/MacOS/3DModelEditor"

# Move JAR to app resources
mv dmg_temp/3d-model-editor.jar "dmg_temp/3D Model Editor.app/Contents/Resources/"

# Create Applications link
ln -sf "/Applications" "dmg_temp/Applications"

# Create README
cat > "dmg_temp/README.txt" << 'EOF'
3D Model Editor
===============

Installation:
1. Drag "3D Model Editor.app" to "Applications" folder
2. Launch from Applications or Launchpad

Requirements:
- macOS 10.13+
- Java 17+

EOF

# Create DMG
echo "Creating DMG..."
DMG_PATH="dist/3D-Model-Editor.dmg"

# Remove old DMG if exists
rm -f "$DMG_PATH"

hdiutil create -volname "3D Model Editor" \
    -srcfolder dmg_temp \
    -ov -format UDZO \
    "$DMG_PATH"

if [ $? -eq 0 ] && [ -f "$DMG_PATH" ]; then
    DMG_SIZE=$(du -h "$DMG_PATH" | cut -f1)
    echo "✅ DMG created successfully!"
    echo "📊 Size: $DMG_SIZE"
    echo "📍 Location: $DMG_PATH"

    # Clean up
    rm -rf dmg_temp

    # Open dist folder in Finder
    echo "📁 Opening dist folder..."
    open dist/
else
    echo "❌ Failed to create DMG!"

    # Try alternative
    echo "Trying alternative method..."
    hdiutil makehybrid -hfs -hfs-volume-name "3D Model Editor" \
        -o "$DMG_PATH" dmg_temp/

    if [ -f "$DMG_PATH" ]; then
        echo "✅ DMG created with alternative method!"
        open dist/
    else
        echo "❌ All methods failed!"
        echo "Creating ZIP instead..."
        cd dmg_temp
        zip -r "../dist/3D-Model-Editor.zip" .
        cd ..
        echo "✅ Created ZIP: dist/3D-Model-Editor.zip"
        open dist/
    fi
fi