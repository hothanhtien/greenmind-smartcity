"use client"

import { useRef, useCallback } from "react"
import { uploadImageToR2 } from "@/services/blog.service"
import { ImageIcon, FolderOpen, Upload } from "lucide-react"

interface Props {
  value: string
  onChange: (html: string) => void
  placeholder?: string
}

/**
 * Simple rich-text editor using contentEditable.
 * - Renders blog HTML as-is (images included with lazy loading)
 * - On paste/drop of an image file → uploads to R2, inserts <img loading="lazy"> at cursor
 */
export function BlogEditor({ value, onChange, placeholder = "Write your blog content here..." }: Props) {
  const editorRef = useRef<HTMLDivElement>(null)
  const uploadingRef = useRef(false)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const folderInputRef = useRef<HTMLInputElement>(null)

  const handleInput = useCallback(() => {
    if (editorRef.current) {
      onChange(editorRef.current.innerHTML)
    }
  }, [onChange])

  const handlePaste = useCallback(
    async (e: React.ClipboardEvent<HTMLDivElement>) => {
      const items = Array.from(e.clipboardData.items)
      const imageItem = items.find((item) => item.type.startsWith("image/"))
      if (!imageItem) return
      e.preventDefault()
      if (uploadingRef.current) return
      uploadingRef.current = true

      const file = imageItem.getAsFile()
      if (!file) { uploadingRef.current = false; return }

      const placeholder = document.createElement("span")
      placeholder.textContent = "Uploading image..."
      placeholder.style.color = "#888"

      const selection = window.getSelection()
      if (selection && selection.rangeCount > 0) {
        const range = selection.getRangeAt(0)
        range.insertNode(placeholder)
        range.collapse(false)
      } else {
        editorRef.current?.appendChild(placeholder)
      }

      try {
        const url = await uploadImageToR2(file)
        const imgEl = document.createElement("img")
        imgEl.src = url
        imgEl.alt = file.name
        imgEl.loading = "lazy"
        imgEl.style.maxWidth = "100%"
        imgEl.style.borderRadius = "8px"
        imgEl.style.margin = "8px 0"
        placeholder.replaceWith(imgEl)
      } catch {
        placeholder.textContent = "Image upload failed"
      } finally {
        uploadingRef.current = false
        handleInput()
      }
    },
    [handleInput]
  )

  const handleDrop = useCallback(
    async (e: React.DragEvent<HTMLDivElement>) => {
      const file = Array.from(e.dataTransfer.files).find((f) => f.type.startsWith("image/"))
      if (!file) return
      e.preventDefault()
      if (uploadingRef.current) return
      uploadingRef.current = true

      try {
        const url = await uploadImageToR2(file)
        const imgHtml = `<img src="${url}" alt="${file.name}" loading="lazy" style="max-width:100%;border-radius:8px;margin:8px 0;" />`
        document.execCommand("insertHTML", false, imgHtml)
      } catch {
        // silent
      } finally {
        uploadingRef.current = false
        handleInput()
      }
    },
    [handleInput]
  )

  const handleInsertImageFromUrl = useCallback(() => {
    const url = prompt("Enter image URL:")
    if (!url) return
    const imgHtml = `<img src="${url}" alt="image" loading="lazy" style="max-width:100%;border-radius:8px;margin:8px 0;" />`
    editorRef.current?.focus()
    document.execCommand("insertHTML", false, imgHtml)
    handleInput()
  }, [handleInput])

  const handleFileUpload = useCallback(
    async (files: FileList | null) => {
      if (!files || files.length === 0) return
      const imageFiles = Array.from(files).filter((f) => f.type.startsWith("image/"))
      if (imageFiles.length === 0) return

      for (const file of imageFiles) {
        if (uploadingRef.current) {
          // wait for previous upload to finish
          await new Promise<void>((resolve) => {
            const check = setInterval(() => {
              if (!uploadingRef.current) { clearInterval(check); resolve() }
            }, 100)
          })
        }
        uploadingRef.current = true
        const placeholder = document.createElement("span")
        placeholder.textContent = `Uploading ${file.name}...`
        placeholder.style.color = "#888"
        editorRef.current?.appendChild(placeholder)

        try {
          const url = await uploadImageToR2(file)
          const imgEl = document.createElement("img")
          imgEl.src = url
          imgEl.alt = file.name
          imgEl.loading = "lazy"
          imgEl.style.maxWidth = "100%"
          imgEl.style.borderRadius = "8px"
          imgEl.style.margin = "8px 0"
          imgEl.style.display = "block"
          placeholder.replaceWith(imgEl)
        } catch {
          placeholder.textContent = `Failed to upload ${file.name}`
        } finally {
          uploadingRef.current = false
          handleInput()
        }
      }
    },
    [handleInput]
  )

  const exec = (cmd: string, value?: string) => {
    editorRef.current?.focus()
    document.execCommand(cmd, false, value)
    handleInput()
  }

  return (
    <div className="flex flex-col rounded-xl border border-border overflow-hidden focus-within:ring-2 focus-within:ring-primary/30">
      {/* Hidden file inputs */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        multiple
        className="hidden"
        onChange={(e) => handleFileUpload(e.target.files)}
      />
      <input
        ref={folderInputRef}
        type="file"
        accept="image/*"
        multiple
        // @ts-expect-error webkitdirectory is non-standard but widely supported
        webkitdirectory=""
        className="hidden"
        onChange={(e) => handleFileUpload(e.target.files)}
      />

      {/* Toolbar */}
      <div className="flex flex-wrap items-center gap-1 border-b border-border bg-muted/40 px-3 py-2">
        {[
          { label: "Bold", cmd: "bold" },
          { label: "Italic", cmd: "italic" },
          { label: "Underline", cmd: "underline" },
        ].map(({ label, cmd }) => (
          <button
            key={cmd}
            type="button"
            onMouseDown={(e) => { e.preventDefault(); exec(cmd) }}
            className="h-7 px-2 rounded text-xs font-semibold text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
          >
            {label}
          </button>
        ))}

        <div className="w-px h-5 bg-border mx-1" />

        {[
          { label: "H2", cmd: "formatBlock", value: "h2" },
          { label: "H3", cmd: "formatBlock", value: "h3" },
          { label: "Paragraph", cmd: "formatBlock", value: "p" },
        ].map(({ label, cmd, value }) => (
          <button
            key={value}
            type="button"
            onMouseDown={(e) => { e.preventDefault(); exec(cmd, value) }}
            className="h-7 px-2 rounded text-xs font-semibold text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
          >
            {label}
          </button>
        ))}

        <div className="w-px h-5 bg-border mx-1" />

        <button
          type="button"
          onMouseDown={(e) => { e.preventDefault(); handleInsertImageFromUrl() }}
          className="h-7 px-2 rounded text-xs text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
        >
          Insert Image
        </button>

        <div className="w-px h-5 bg-border mx-1" />

        <button
          type="button"
          onMouseDown={(e) => { e.preventDefault(); fileInputRef.current?.click() }}
          className="h-7 px-2 rounded text-xs text-muted-foreground hover:bg-muted hover:text-foreground transition-colors inline-flex items-center gap-1"
          title="Select image files to upload"
        >
          <Upload className="h-3 w-3" />
          Upload Files
        </button>

        <button
          type="button"
          onMouseDown={(e) => { e.preventDefault(); folderInputRef.current?.click() }}
          className="h-7 px-2 rounded text-xs text-muted-foreground hover:bg-muted hover:text-foreground transition-colors inline-flex items-center gap-1"
          title="Select a folder to upload all images from it"
        >
          <FolderOpen className="h-3 w-3" />
          Select Folder
        </button>
      </div>

      {/* Content area */}
      <div
        ref={editorRef}
        id="blog-editor"
        contentEditable
        suppressContentEditableWarning
        onInput={handleInput}
        onPaste={handlePaste}
        onDrop={handleDrop}
        onDragOver={(e) => e.preventDefault()}
        data-placeholder={placeholder}
        className="min-h-[280px] p-4 text-sm text-foreground outline-none prose prose-sm max-w-none
          [&_img]:max-w-full [&_img]:rounded-lg [&_img]:my-2
          [&_h2]:text-xl [&_h2]:font-bold [&_h2]:mt-4 [&_h2]:mb-2
          [&_h3]:text-lg [&_h3]:font-semibold [&_h3]:mt-3 [&_h3]:mb-1.5
          [&_p]:mb-2 [&_ul]:list-disc [&_ul]:pl-5 [&_ol]:list-decimal [&_ol]:pl-5
          empty:before:content-[attr(data-placeholder)] empty:before:text-muted-foreground/60 empty:before:pointer-events-none"
        dangerouslySetInnerHTML={{ __html: value }}
      />
    </div>
  )
}
