import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.File
import java.io.IOException

class PdfThumbnailUtilsTest {

    @Mock
    private lateinit var file: File

    @Mock
    private lateinit var fileDescriptor: ParcelFileDescriptor

    @Mock
    private lateinit var pdfRenderer: PdfRenderer

    @Mock
    private lateinit var pdfPage: PdfRenderer.Page

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `generatePDFThumbnail returns bitmap on valid page`() {
        Mockito.`when`(file.exists()).thenReturn(true)
        Mockito.`when`(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).thenReturn(fileDescriptor)
        Mockito.`when`(pdfRenderer.pageCount).thenReturn(1)
        Mockito.`when`(pdfRenderer.openPage(0)).thenReturn(pdfPage)
        Mockito.`when`(pdfPage.width).thenReturn(100)
        Mockito.`when`(pdfPage.height).thenReturn(100)

        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        Mockito.`when`(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)).thenReturn(mockBitmap)

        val bitmap = PdfThumbnailUtils.generatePDFThumbnail(file, 0)

        assertNotNull(bitmap)
        assertEquals(mockBitmap.width, bitmap?.width)
        assertEquals(mockBitmap.height, bitmap?.height)

        Mockito.verify(pdfPage).render(mockBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        Mockito.verify(pdfPage).close()
        Mockito.verify(pdfRenderer).close()
        Mockito.verify(fileDescriptor).close()
    }

    @Test
    fun `generatePDFThumbnail returns null when page index is out of range`() {
        Mockito.`when`(file.exists()).thenReturn(true)
        Mockito.`when`(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).thenReturn(fileDescriptor)
        Mockito.`when`(pdfRenderer.pageCount).thenReturn(1)

        val bitmap = PdfThumbnailUtils.generatePDFThumbnail(file, 5) // Invalid page index

        assertNull(bitmap)
        Mockito.verify(pdfRenderer, Mockito.never()).openPage(Mockito.anyInt())
    }

    @Test
    fun `generatePDFThumbnail returns null on IOException`() {
        Mockito.`when`(file.exists()).thenReturn(true)
        Mockito.`when`(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
            .thenThrow(IOException::class.java)

        val bitmap = PdfThumbnailUtils.generatePDFThumbnail(file, 0)

        assertNull(bitmap)
    }
}
